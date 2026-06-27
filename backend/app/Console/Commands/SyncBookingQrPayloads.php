<?php

namespace App\Console\Commands;

use App\Models\Booking;
use App\Services\BookingQrService;
use Illuminate\Console\Command;

class SyncBookingQrPayloads extends Command
{
    protected $signature = 'parkshare:sync-booking-qr';

    protected $description = 'Rebuild structured JSON QR payloads for all bookings';

    public function handle(BookingQrService $qrService): int
    {
        $count = 0;

        Booking::query()->chunkById(100, function ($bookings) use ($qrService, &$count) {
            foreach ($bookings as $booking) {
                $qrService->syncStoredQr($booking);
                $count++;
            }
        });

        $this->info("Synced {$count} booking QR payload(s).");

        return self::SUCCESS;
    }
}
