<?php

namespace App\Console\Commands;

use App\Models\ParkingSpace;
use App\Services\ParkingSlotService;
use Illuminate\Console\Command;

class SyncParkingSlots extends Command
{
    protected $signature = 'parkshare:sync-slots';

    protected $description = 'Sync parking slot records for all parking spaces';

    public function handle(ParkingSlotService $service): int
    {
        ParkingSpace::query()->each(function (ParkingSpace $space) use ($service) {
            $service->syncSlots($space);
            $this->line("Synced slots for: {$space->parking_name}");
        });

        $this->info('Done.');

        return self::SUCCESS;
    }
}
