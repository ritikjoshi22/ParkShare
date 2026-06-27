<?php

namespace App\Services;

use App\Models\Booking;
use Illuminate\Support\Str;
use Illuminate\Validation\ValidationException;

class BookingQrService
{
    public const VERSION = 1;

    public function buildPayload(Booking $booking): string
    {
        $payload = [
            'v' => self::VERSION,
            'booking_id' => $booking->id,
            'driver_id' => $booking->user_id,
            'parking_space_id' => $booking->parking_space_id,
            // Removed status fields to prevent mismatch errors after payment or check-in
        ];

        return json_encode($payload, JSON_THROW_ON_ERROR);
    }

    public function resolveCheckInStatus(Booking $booking): string
    {
        if ($booking->checked_out_at) {
            return 'checked_out';
        }

        if ($booking->checked_in_at || $booking->booking_status === 'checked_in') {
            return 'checked_in';
        }

        return 'not_checked_in';
    }

    /**
     * @return array<string, mixed>
     */
    public function parse(string $raw): array
    {
        $trimmed = trim($raw);

        if ($trimmed === '') {
            throw ValidationException::withMessages([
                'qr_payload' => ['QR code is empty.'],
            ]);
        }

        $decoded = json_decode($trimmed, true);

        if (is_array($decoded) && isset($decoded['booking_id'])) {
            return $decoded;
        }

        // Legacy UUID stored in qr_code column
        if (Str::isUuid($trimmed)) {
            $booking = Booking::query()->where('qr_code', $trimmed)->first();
            if ($booking) {
                return json_decode($this->buildPayload($booking), true, 512, JSON_THROW_ON_ERROR);
            }
        }

        throw ValidationException::withMessages([
            'qr_payload' => ['Invalid QR code format.'],
        ]);
    }

    public function syncStoredQr(Booking $booking): Booking
    {
        $booking->update(['qr_code' => $this->buildPayload($booking)]);

        return $booking->fresh();
    }

    public function assertPayloadMatchesBooking(array $payload, Booking $booking): void
    {
        $errors = [];

        if ((int) ($payload['booking_id'] ?? 0) !== (int) $booking->id) {
            $errors['qr_payload'][] = 'Booking ID mismatch.';
        }

        if ((int) ($payload['driver_id'] ?? 0) !== (int) $booking->user_id) {
            $errors['qr_payload'][] = 'Driver ID mismatch.';
        }

        if ((int) ($payload['parking_space_id'] ?? 0) !== (int) $booking->parking_space_id) {
            $errors['qr_payload'][] = 'Parking space ID mismatch.';
        }

        if ($errors !== []) {
            throw ValidationException::withMessages($errors);
        }
    }
}
