<?php

namespace App\Filament\Resources\ParkingSpaces\Pages;

use App\Filament\Resources\ParkingSpaces\ParkingSpaceResource;
use Filament\Actions\DeleteAction;
use Filament\Actions\ViewAction;
use Filament\Resources\Pages\EditRecord;

class EditParkingSpace extends EditRecord
{
    protected static string $resource = ParkingSpaceResource::class;

    protected function getHeaderActions(): array
    {
        return [
            ViewAction::make(),
            DeleteAction::make(),
        ];
    }
}
