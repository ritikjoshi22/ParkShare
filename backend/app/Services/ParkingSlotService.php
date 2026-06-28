<?php

namespace App\Services;

use App\Models\ParkingSpace;
use App\Models\ParkingSlot;

class ParkingSlotService
{
    public function syncSlots(ParkingSpace $parking): void
    {
        $total = max(1, (int) $parking->total_slots);
        $existing = $parking->slots()->count();

        if ($existing < $total) {
            for ($i = $existing + 1; $i <= $total; $i++) {
                ParkingSlot::create([
                    'parking_space_id' => $parking->id,
                    'slot_number' => $i,
                    'label' => 'Slot '.$i,
                    'status' => 'available',
                    'sort_order' => $i,
                ]);
            }
        } elseif ($existing > $total) {
            $parking->slots()
                ->where('slot_number', '>', $total)
                ->whereDoesntHave('bookings', fn ($q) => $q->whereNotIn('booking_status', ['cancelled', 'completed']))
                ->delete();
        }

        $parking->update(['available_slots' => $this->countAvailable($parking)]);
    }

    public function countAvailable(ParkingSpace $parking): int
    {
        return $parking->slots()
            ->where('status', '!=', 'maintenance')
            ->count();
    }
}
