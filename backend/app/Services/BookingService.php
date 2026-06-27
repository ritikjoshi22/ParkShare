<?php

namespace App\Services;

use App\Models\Booking;
use App\Models\ParkingAvailability;
use App\Models\ParkingSlot;
use App\Models\ParkingSpace;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class BookingService
{
    public function __construct(
        protected ParkingSlotService $slotService,
        protected BookingQrService $qrService,
        protected SystemSettingsService $settings,
        protected PricingEngine $pricingEngine,
        protected PaymentService $paymentService,
        protected BookingValidationService $validationService
    ) {}

    /**
     * @return array{total: float, hours: float, breakdown: array, start_time: string, end_time: string}
     */
    public function quote(User $user, array $data): array
    {
        $parking = ParkingSpace::active()->findOrFail($data['parking_space_id']);
        $start = Carbon::parse($data['start_time']);
        $end = Carbon::parse($data['end_time']);

        $this->validateBookingWindow($parking, $start, $end, $data['parking_slot_id'] ?? null);

        $pricing = $this->pricingEngine->calculate($parking, $start, $end);

        return array_merge($pricing, [
            'start_time' => $start->toIso8601String(),
            'end_time' => $end->toIso8601String(),
            'parking_space_id' => $parking->id,
        ]);
    }

    public function create(User $user, array $data): Booking
    {
        $parking = ParkingSpace::active()->findOrFail($data['parking_space_id']);
        $start = Carbon::parse($data['start_time']);
        $end = Carbon::parse($data['end_time']);
        $slotId = $data['parking_slot_id'] ?? null;

        $this->validateBookingWindow($parking, $start, $end, $slotId);

        $pricing = $this->pricingEngine->calculate($parking, $start, $end);

        return DB::transaction(function () use ($user, $parking, $start, $end, $pricing, $slotId) {
            $booking = Booking::create([
                'user_id' => $user->id,
                'parking_space_id' => $parking->id,
                'parking_slot_id' => $slotId,
                'booking_date' => $start->toDateString(),
                'start_time' => $start,
                'end_time' => $end,
                'original_end_time' => $end,
                'total_hours' => $pricing['hours'],
                'total_amount' => $pricing['total'],
                'amount_due' => $pricing['total'],
                'payment_status' => 'pending',
                'booking_status' => 'pending',
                'qr_code' => null,
            ]);

            if ($slotId) {
                ParkingSlot::where('id', $slotId)->update(['status' => 'reserved']);
            }

            if ($parking->available_slots > 0) {
                $parking->decrement('available_slots');
            }

            return $booking->load(['parkingSpace.images', 'user', 'parkingSlot']);
        });
    }

    public function cancel(Booking $booking, User $user): Booking
    {
        if ($booking->booking_status === 'cancelled') {
            throw ValidationException::withMessages(['booking' => ['Booking is already cancelled.']]);
        }

        if (in_array($booking->booking_status, ['checked_in', 'checked_out', 'completed'], true)) {
            throw ValidationException::withMessages(['booking' => ['Bookings that have started cannot be cancelled.']]);
        }

        return DB::transaction(function () use ($booking) {
            $booking->update(['booking_status' => 'cancelled']);
            $this->releaseSlot($booking);

            $parking = $booking->parkingSpace;
            if ($parking && $parking->available_slots < $parking->total_slots) {
                $parking->increment('available_slots');
            }

            return $booking->fresh(['parkingSpace', 'user', 'parkingSlot']);
        });
    }

    /**
     * @return array{action: string, message: string, booking: Booking, payment_required?: bool, amount_due?: float}
     */
    public function scanQr(User $owner, string $rawPayload): array
    {
        if (! $owner->isOwner()) {
            abort(403, 'Only parking owners can scan booking QR codes.');
        }

        $payload = $this->qrService->parse($rawPayload);
        $booking = Booking::with(['parkingSpace', 'parkingSlot', 'user'])->find($payload['booking_id'] ?? 0);

        if (! $booking) {
            throw ValidationException::withMessages(['qr_payload' => ['Booking not found.']]);
        }

        $this->qrService->assertPayloadMatchesBooking($payload, $booking);
        $this->assertOwnerOwnsParking($owner, $booking);

        if (in_array($booking->booking_status, ['pending', 'confirmed'])) {
            if ($booking->booking_status === 'pending') {
                $due = (float) $booking->amount_due;
                return [
                    'action' => 'payment_required',
                    'message' => 'Booking not confirmed. Payment of NPR ' . number_format($due, 2) . ' is pending.',
                    'booking' => $booking,
                    'payment_required' => true,
                    'amount_due' => $due,
                ];
            }

            if (! $booking->checked_in_at) {
                $booking = $this->performOwnerCheckIn($booking);

                return [
                    'action' => 'check_in',
                    'message' => 'Driver checked in successfully.',
                    'booking' => $booking,
                ];
            }
        }

        if ($booking->booking_status === 'checked_in' && $booking->checked_in_at && ! $booking->checked_out_at) {
            $now = Carbon::now();
            $overtimeFee = $this->pricingEngine->calculateOvertime(
                $booking->parkingSpace,
                $booking->end_time,
                $now
            );

            if ($overtimeFee > 0 && (float) $booking->overtime_fee < $overtimeFee) {
                $booking->update([
                    'overtime_fee' => $overtimeFee,
                    'amount_due' => (float) $booking->amount_due + ($overtimeFee - (float) $booking->overtime_fee),
                    'payment_status' => $booking->payment_status === 'paid' ? 'partial' : $booking->payment_status,
                ]);
                $booking = $booking->fresh(['parkingSpace', 'parkingSlot', 'user']);
            }

            $due = (float) $booking->amount_due;
            if ($due > 0 && $booking->payment_status !== 'paid') {
                return [
                    'action' => 'payment_required',
                    'message' => 'Payment pending. Amount due: NPR '.number_format($due, 2),
                    'booking' => $booking,
                    'payment_required' => true,
                    'amount_due' => $due,
                ];
            }

            $booking = $this->performOwnerCheckOut($booking);

            return [
                'action' => 'check_out',
                'message' => 'Driver checked out successfully. Booking completed.',
                'booking' => $booking,
            ];
        }

        throw $this->invalidScanException($booking);
    }

    protected function performOwnerCheckIn(Booking $booking): Booking
    {
        if ($booking->booking_status !== 'confirmed') {
            throw ValidationException::withMessages([
                'booking' => ['Only confirmed bookings can be checked in.'],
            ]);
        }

        if ($booking->checked_in_at) {
            throw ValidationException::withMessages(['booking' => ['Driver has already checked in.']]);
        }

        $checkResult = $this->validationService->canCheckIn($booking);

        if (! $checkResult['allowed']) {
            throw ValidationException::withMessages([
                'booking' => [$checkResult['message']],
            ]);
        }

        $now = Carbon::now();

        return DB::transaction(function () use ($booking, $now) {
            $booking->update([
                'checked_in_at' => $now,
                'booking_status' => 'checked_in',
            ]);

            if ($booking->parking_slot_id) {
                ParkingSlot::where('id', $booking->parking_slot_id)->update(['status' => 'occupied']);
            }

            return $this->qrService->syncStoredQr(
                $booking->fresh(['parkingSpace.images', 'user', 'parkingSlot'])
            );
        });
    }

    protected function performOwnerCheckOut(Booking $booking): Booking
    {
        $checkResult = $this->validationService->canCheckOut($booking);

        if (! $checkResult['allowed']) {
            throw ValidationException::withMessages([
                'booking' => [$checkResult['message']],
            ]);
        }

        $now = Carbon::now();
        $overtimeFee = (float) $booking->overtime_fee;

        return DB::transaction(function () use ($booking, $now, $overtimeFee) {
            $booking->update([
                'checked_out_at' => $now,
                'booking_status' => 'checked_out',
                'overtime_fee' => $overtimeFee,
                'total_amount' => (float) $booking->total_amount + $overtimeFee,
            ]);

            $booking->update(['booking_status' => 'completed']);

            $this->releaseSlot($booking);

            $parking = $booking->parkingSpace;
            if ($parking && $parking->available_slots < $parking->total_slots) {
                $parking->increment('available_slots');
            }

            return $this->qrService->syncStoredQr(
                $booking->fresh(['parkingSpace.images', 'user', 'parkingSlot'])
            );
        });
    }

    protected function validateBookingWindow(ParkingSpace $parking, Carbon $start, Carbon $end, ?int $slotId): void
    {
        if ($end->lte($start)) {
            throw ValidationException::withMessages(['end_time' => ['End time must be after start time.']]);
        }

        $this->assertWithinOperatingHours($parking, $start, $end);
        $this->assertAvailabilitySchedule($parking, $start, $end);
        $this->assertNoOverlap($parking, $start, $end, $slotId);
        $this->assertSlotsAvailable($parking, $slotId);
    }

    protected function assertOwnerOwnsParking(User $owner, Booking $booking): void
    {
        $parking = $booking->parkingSpace;
        if (! $parking || (int) $parking->owner_id !== (int) $owner->id) {
            throw ValidationException::withMessages([
                'qr_payload' => ['This booking does not belong to your parking space.'],
            ]);
        }
    }

    protected function invalidScanException(Booking $booking): ValidationException
    {
        return match ($booking->booking_status) {
            'completed' => ValidationException::withMessages(['booking' => ['This booking is already completed.']]),
            'cancelled' => ValidationException::withMessages(['booking' => ['This booking was cancelled.']]),
            default => ValidationException::withMessages(['booking' => ['Cannot process scan for the current booking state.']]),
        };
    }

    protected function releaseSlot(Booking $booking): void
    {
        if ($booking->parking_slot_id) {
            ParkingSlot::where('id', $booking->parking_slot_id)->update(['status' => 'available']);
        }
    }

    protected function assertWithinOperatingHours(ParkingSpace $parking, Carbon $start, Carbon $end): void
    {
        $open = Carbon::parse($start->toDateString().' '.$parking->opening_time);
        $close = Carbon::parse($start->toDateString().' '.$parking->closing_time);

        if ($start->lt($open)) {
            throw ValidationException::withMessages([
                'start_time' => ['Booking cannot start before opening time ('.$parking->opening_time.').'],
            ]);
        }

        if ($end->gt($close)) {
            throw ValidationException::withMessages([
                'end_time' => ['Booking cannot end after closing time ('.$parking->closing_time.').'],
            ]);
        }
    }

    protected function assertAvailabilitySchedule(ParkingSpace $parking, Carbon $start, Carbon $end): void
    {
        $date = $start->toDateString();
        $slots = ParkingAvailability::where('parking_space_id', $parking->id)
            ->where('available_date', $date)
            ->where('is_available', true)
            ->get();

        if ($slots->isEmpty()) {
            return;
        }

        $fits = $slots->contains(function ($slot) use ($start, $end) {
            $slotStart = Carbon::parse($start->toDateString().' '.$slot->start_time);
            $slotEnd = Carbon::parse($start->toDateString().' '.$slot->end_time);

            return $start->gte($slotStart) && $end->lte($slotEnd);
        });

        if (! $fits) {
            throw ValidationException::withMessages([
                'start_time' => ['Selected time is outside defined availability windows for this date.'],
            ]);
        }
    }

    protected function assertNoOverlap(ParkingSpace $parking, Carbon $start, Carbon $end, ?int $slotId): void
    {
        $buffer = $parking->booking_buffer_minutes ?? $this->settings->getInt('booking_buffer_minutes');
        $bufferedStart = $start->copy()->subMinutes($buffer);
        $bufferedEnd = $end->copy()->addMinutes($buffer);

        $baseQuery = Booking::forParking($parking->id)
            ->whereNotIn('booking_status', ['cancelled', 'completed'])
            ->where('start_time', '<', $bufferedEnd)
            ->where('end_time', '>', $bufferedStart);

        if ($slotId) {
            if ((clone $baseQuery)->where('parking_slot_id', $slotId)->exists()) {
                throw ValidationException::withMessages([
                    'parking_slot_id' => ['This slot is already booked for the selected time (including buffer).'],
                ]);
            }

            return;
        }

        $overlapCount = (clone $baseQuery)->count();
        if ($overlapCount >= max(1, $parking->total_slots)) {
            throw ValidationException::withMessages([
                'start_time' => ['No slots available for the selected time.'],
            ]);
        }
    }

    protected function assertSlotsAvailable(ParkingSpace $parking, ?int $slotId): void
    {
        if ($parking->available_slots < 1) {
            throw ValidationException::withMessages([
                'parking_space_id' => ['No available slots at this parking space.'],
            ]);
        }

        if ($slotId) {
            $slot = ParkingSlot::where('parking_space_id', $parking->id)->find($slotId);
            if (! $slot || $slot->status === 'maintenance') {
                throw ValidationException::withMessages([
                    'parking_slot_id' => ['Selected slot is disabled or under maintenance.'],
                ]);
            }
        }
    }
}
