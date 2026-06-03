<?php

namespace App\Filament\Resources\ParkingSpaces\Pages;

use App\Filament\Resources\ParkingSpaces\ParkingSpaceResource;
use Filament\Actions\EditAction;
use Filament\Resources\Pages\ViewRecord;

class ViewParkingSpace extends ViewRecord
{
    protected static string $resource = ParkingSpaceResource::class;

    protected function getHeaderActions(): array
    {
        return [
            EditAction::make(),
        ];
    }
}
