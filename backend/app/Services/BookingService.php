<?php

namespace App\Services;

use App\Models\Booking;
use App\Models\ParkingAvailability;
use App\Models\ParkingSpace;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

class BookingService
{
    public function create(User $user, array $data): Booking
    {
        $parking = ParkingSpace::active()->findOrFail($data['parking_space_id']);

        $start = Carbon::parse($data['start_time']);
        $end = Carbon::parse($data['end_time']);

        if ($end->lte($start)) {
            throw ValidationException::withMessages([
                'end_time' => ['End time must be after start time.'],
            ]);
        }

        $this->assertWithinOperatingHours($parking, $start, $end);
        $this->assertAvailabilitySchedule($parking, $start, $end);
        $this->assertNoOverlap($parking->id, $start, $end);
        $this->assertSlotsAvailable($parking);

        $hours = round($start->diffInMinutes($end) / 60, 2);
        $amount = round($hours * (float) $parking->price_per_hour, 2);

        return DB::transaction(function () use ($user, $parking, $data, $start, $end, $hours, $amount) {
            $booking = Booking::create([
                'user_id' => $user->id,
                'parking_space_id' => $parking->id,
                'booking_date' => $start->toDateString(),
                'start_time' => $start,
                'end_time' => $end,
                'total_hours' => $hours,
                'total_amount' => $amount,
                'booking_status' => 'confirmed',
                'qr_code' => Str::uuid()->toString(),
            ]);

            if ($parking->available_slots > 0) {
                $parking->decrement('available_slots');
            }

            return $booking->load(['parkingSpace.images', 'user']);
        });
    }

    public function cancel(Booking $booking, User $user): Booking
    {
        if ($booking->booking_status === 'cancelled') {
            throw ValidationException::withMessages([
                'booking' => ['Booking is already cancelled.'],
            ]);
        }

        if ($booking->booking_status === 'completed') {
            throw ValidationException::withMessages([
                'booking' => ['Completed bookings cannot be cancelled.'],
            ]);
        }

        return DB::transaction(function () use ($booking) {
            $booking->update(['booking_status' => 'cancelled']);

            $parking = $booking->parkingSpace;
            if ($parking && $parking->available_slots < $parking->total_slots) {
                $parking->increment('available_slots');
            }

            return $booking->fresh(['parkingSpace', 'user']);
        });
    }

    protected function assertWithinOperatingHours(ParkingSpace $parking, Carbon $start, Carbon $end): void
    {
        $open = Carbon::parse($start->toDateString().' '.$parking->opening_time);
        $close = Carbon::parse($start->toDateString().' '.$parking->closing_time);

        if ($start->lt($open) || $end->gt($close)) {
            throw ValidationException::withMessages([
                'start_time' => ['Booking must be within parking operating hours.'],
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

    protected function assertNoOverlap(int $parkingSpaceId, Carbon $start, Carbon $end): void
    {
        $overlap = Booking::forParking($parkingSpaceId)
            ->whereNotIn('booking_status', ['cancelled'])
            ->where('start_time', '<', $end)
            ->where('end_time', '>', $start)
            ->exists();

        if ($overlap) {
            throw ValidationException::withMessages([
                'start_time' => ['This parking space is already booked for the selected time slot.'],
            ]);
        }
    }

    protected function assertSlotsAvailable(ParkingSpace $parking): void
    {
        if ($parking->available_slots < 1) {
            throw ValidationException::withMessages([
                'parking_space_id' => ['No available slots at this parking space.'],
            ]);
        }
    }
}
