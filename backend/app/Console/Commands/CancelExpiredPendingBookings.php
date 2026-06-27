<?php

namespace App\Console\Commands;

use App\Models\Booking;
use App\Models\ParkingSlot;
use App\Models\ParkingSpace;
use Carbon\Carbon;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class CancelExpiredPendingBookings extends Command
{
    protected $signature = 'bookings:cancel-expired';
    protected $description = 'Cancel pending bookings that were not paid within 15 minutes';

    public function handle()
    {
        $expiryTime = Carbon::now()->subMinutes(15);

        $expiredBookings = Booking::where('booking_status', 'pending')
            ->where('created_at', '<=', $expiryTime)
            ->get();

        if ($expiredBookings->isEmpty()) {
            return;
        }

        $this->info("Found {$expiredBookings->count()} expired pending bookings.");

        foreach ($expiredBookings as $booking) {
            DB::transaction(function () use ($booking) {
                $booking->update(['booking_status' => 'cancelled']);

                // Release slot
                if ($booking->parking_slot_id) {
                    ParkingSlot::where('id', $booking->parking_slot_id)->update(['status' => 'available']);
                }

                // Increment available slots in parking space
                $parking = $booking->parkingSpace;
                if ($parking && $parking->available_slots < $parking->total_slots) {
                    $parking->increment('available_slots');
                }

                Log::info("Booking #{$booking->id} automatically cancelled due to payment timeout.");
            });
        }

        $this->info("Expired bookings cancelled successfully.");
    }
}
