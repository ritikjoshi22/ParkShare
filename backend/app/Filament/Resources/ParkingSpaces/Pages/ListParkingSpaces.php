<?php

namespace App\Filament\Resources\ParkingSpaces\Pages;

use App\Filament\Resources\ParkingSpaces\ParkingSpaceResource;
use Filament\Actions\CreateAction;
use Filament\Resources\Pages\ListRecords;

class ListParkingSpaces extends ListRecords
{
    protected static string $resource = ParkingSpaceResource::class;

    protected function getHeaderActions(): array
    {
        return [
            CreateAction::make(),
        ];
    }
}
