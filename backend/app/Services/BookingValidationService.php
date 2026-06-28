<?php

namespace App\Services;

use App\Models\Booking;
use Carbon\Carbon;
use Illuminate\Support\Facades\Log;

class BookingValidationService
{
    public function __construct(protected SystemSettingsService $settings) {}

    public function canCheckIn(Booking $booking, Carbon $now = null): array
    {
        $now = $now ?: Carbon::now();

        $earlyBuffer = $booking->parkingSpace->early_check_in_minutes
            ?? $this->settings->getInt('early_check_in_buffer_minutes', 15);
        $lateGrace = $this->settings->getInt('late_check_in_grace_minutes', 15);

        $earliest = $booking->start_time->copy()->subMinutes($earlyBuffer);
        $latest = $booking->start_time->copy()->addMinutes($lateGrace);

        $logData = [
            'booking_id' => $booking->id,
            'current_time' => $now->toDateTimeString(),
            'current_timezone' => $now->timezoneName,
            'booking_start' => $booking->start_time->toDateTimeString(),
            'booking_end' => $booking->end_time->toDateTimeString(),
            'early_buffer' => $earlyBuffer,
            'late_grace' => $lateGrace,
            'allowed_earliest' => $earliest->toDateTimeString(),
            'allowed_latest' => $latest->toDateTimeString(),
        ];

        if ($now->lt($earliest)) {
            Log::info("Check-in REJECTED: Too early", array_merge($logData, ['reason' => 'lt_earliest']));
            return [
                'allowed' => false,
                'message' => 'Check-in opens at ' . $earliest->format('g:i A') . ' (' . $earlyBuffer . ' min before booking).',
                'code' => 'TOO_EARLY'
            ];
        }

        if ($now->gt($latest)) {
            Log::info("Check-in REJECTED: Too late (grace expired)", array_merge($logData, ['reason' => 'gt_latest']));
            return [
                'allowed' => false,
                'message' => 'Check-in window closed. Late grace was ' . $lateGrace . ' minutes after start.',
                'code' => 'TOO_LATE'
            ];
        }

        if ($now->gt($booking->end_time)) {
            Log::info("Check-in REJECTED: Booking expired", array_merge($logData, ['reason' => 'gt_end_time']));
            return [
                'allowed' => false,
                'message' => 'Booking time has expired.',
                'code' => 'EXPIRED'
            ];
        }

        Log::info("Check-in ALLOWED", $logData);
        return ['allowed' => true, 'message' => 'Check-in allowed.'];
    }

    public function canCheckOut(Booking $booking, Carbon $now = null): array
    {
        $now = $now ?: Carbon::now();

        if ($booking->booking_status !== 'checked_in' || !$booking->checked_in_at) {
            return [
                'allowed' => false,
                'message' => 'Driver must be checked in before check-out.',
                'code' => 'NOT_CHECKED_IN'
            ];
        }

        if ($booking->checked_out_at) {
            return [
                'allowed' => false,
                'message' => 'Driver has already checked out.',
                'code' => 'ALREADY_CHECKED_OUT'
            ];
        }

        return ['allowed' => true, 'message' => 'Check-out allowed.'];
    }

    public function canExtend(Booking $booking): array
    {
        if (! in_array($booking->booking_status, ['confirmed', 'checked_in'], true)) {
            return [
                'allowed' => false,
                'message' => 'Only active bookings can be extended.',
                'code' => 'INVALID_STATUS'
            ];
        }

        if ($booking->checked_out_at || $booking->booking_status === 'completed') {
            return [
                'allowed' => false,
                'message' => 'Cannot extend a completed booking.',
                'code' => 'ALREADY_COMPLETED'
            ];
        }

        return ['allowed' => true, 'message' => 'Extension possible.'];
    }

    public function validateBookingWindow(\App\Models\ParkingSpace $parking, Carbon $start, Carbon $end, ?int $slotId = null): void
    {
        if ($end->lte($start)) {
            throw \Illuminate\Validation\ValidationException::withMessages(['end_time' => ['End time must be after start time.']]);
        }

        $this->assertWithinOperatingHours($parking, $start, $end);
    }

    protected function assertWithinOperatingHours(\App\Models\ParkingSpace $parking, Carbon $start, Carbon $end): void
    {
        $open = Carbon::parse($start->toDateString() . ' ' . $parking->opening_time);
        $close = Carbon::parse($start->toDateString() . ' ' . $parking->closing_time);

        if ($start->lt($open)) {
            throw \Illuminate\Validation\ValidationException::withMessages([
                'start_time' => ['Booking cannot start before opening time (' . $parking->opening_time . ').'],
            ]);
        }

        if ($end->gt($close)) {
            throw \Illuminate\Validation\ValidationException::withMessages([
                'end_time' => ['Booking cannot end after closing time (' . $parking->closing_time . ').'],
            ]);
        }
    }

    public function calculateOvertime(Booking $booking, Carbon $now = null): float
    {
        $now = $now ?: Carbon::now();
        if ($now->lte($booking->end_time)) {
            return 0.0;
        }

        // Logic could be moved from PricingEngine if needed, but for now we centralize the check
        return app(PricingEngine::class)->calculateOvertime(
            $booking->parkingSpace,
            $booking->end_time,
            $now
        );
    }
}
