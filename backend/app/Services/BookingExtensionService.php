<?php

namespace App\Services;

use App\Models\Booking;
use App\Models\ParkingSpace;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class BookingExtensionService
{
    public function __construct(
        protected SystemSettingsService $settings,
        protected PricingEngine $pricingEngine,
        protected BookingValidationService $validationService
    ) {}

    /**
     * Optimized method to calculate possible extension window.
     */
    public function getExtensionOptions(Booking $booking): array
    {
        $check = $this->validationService->canExtend($booking);
        if (!$check['allowed']) {
            return [
                'can_extend' => false,
                'max_end_time' => null,
                'max_minutes' => 0,
                'reason' => $check['message'],
                'next_booking_start' => null,
            ];
        }

        $parking = $booking->parkingSpace;
        $buffer = $this->resolveBuffer($parking);
        $currentEnd = $booking->end_time->copy();

        // 1. Find the very next booking for this slot (or any slot if not assigned)
        $nextBooking = Booking::forParking($parking->id)
            ->where('id', '!=', $booking->id)
            ->whereNotIn('booking_status', ['cancelled', 'completed'])
            ->where('start_time', '>=', $currentEnd->copy()->subMinutes($buffer)) // Include buffer in search
            ->when($booking->parking_slot_id, function ($q) use ($booking) {
                return $q->where('parking_slot_id', $booking->parking_slot_id);
            })
            ->orderBy('start_time')
            ->first();

        // 2. Consider operating hours (cannot extend past closing)
        $closingTime = Carbon::parse($currentEnd->toDateString() . ' ' . $parking->closing_time);

        $maxPossibleEnd = $closingTime;

        if ($nextBooking) {
            $nextStartWithBuffer = $nextBooking->start_time->copy()->subMinutes($buffer);
            if ($nextStartWithBuffer->lt($maxPossibleEnd)) {
                $maxPossibleEnd = $nextStartWithBuffer;
            }
        }

        $maxMinutes = (int) $currentEnd->diffInMinutes($maxPossibleEnd, false);

        if ($maxMinutes < 15) {
            return [
                'can_extend' => false,
                'max_end_time' => null,
                'max_minutes' => 0,
                'reason' => $nextBooking
                    ? 'Another booking starts soon at ' . $nextBooking->start_time->format('g:i A')
                    : 'Parking closes at ' . $parking->closing_time,
                'next_booking_start' => $nextBooking?->start_time?->toIso8601String(),
            ];
        }

        return [
            'can_extend' => true,
            'max_end_time' => $maxPossibleEnd->toIso8601String(),
            'max_minutes' => $maxMinutes,
            'reason' => 'You can extend up to ' . $maxMinutes . ' minutes.',
            'next_booking_start' => $nextBooking?->start_time?->toIso8601String(),
        ];
    }

    /**
     * Optimized extension execution.
     */
    public function extend(Booking $booking, int $minutes): Booking
    {
        $options = $this->getExtensionOptions($booking);

        if (!$options['can_extend']) {
            throw ValidationException::withMessages(['minutes' => [$options['reason']]]);
        }

        if ($minutes > $options['max_minutes']) {
            throw ValidationException::withMessages([
                'minutes' => ['Maximum extension allowed is ' . $options['max_minutes'] . ' minutes.']
            ]);
        }

        if ($minutes < 15 || $minutes % 15 !== 0) {
            throw ValidationException::withMessages(['minutes' => ['Extension must be in 15-minute blocks.']]);
        }

        $parking = $booking->parkingSpace;
        $oldEnd = $booking->end_time->copy();
        $newEnd = $oldEnd->copy()->addMinutes($minutes);

        // Pricing for the additional time only
        $pricing = $this->pricingEngine->calculate($parking, $oldEnd, $newEnd);
        $extraCost = (float) $pricing['total'];

        return DB::transaction(function () use ($booking, $newEnd, $minutes, $extraCost) {
            $booking->update([
                'end_time' => $newEnd,
                'original_end_time' => $booking->original_end_time ?: $booking->getOriginal('end_time'),
                'extended_minutes' => $booking->extended_minutes + $minutes,
                'total_amount' => round((float) $booking->total_amount + $extraCost, 2),
                'amount_due' => round((float) $booking->amount_due + $extraCost, 2),
                'payment_status' => $booking->payment_status === 'paid' ? 'partial' : $booking->payment_status,
            ]);

            $booking = $booking->fresh(['parkingSpace', 'parkingSlot', 'user']);

            return $booking;
        });
    }

    protected function resolveBuffer(ParkingSpace $parking): int
    {
        return (int) ($parking->booking_buffer_minutes ?? $this->settings->getInt('booking_buffer_minutes', 15));
    }
}
