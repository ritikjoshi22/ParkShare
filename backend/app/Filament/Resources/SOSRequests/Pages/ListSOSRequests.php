<?php

namespace App\Filament\Resources\SOSRequests\Pages;

use App\Filament\Resources\SOSRequests\SOSRequestResource;
use Filament\Actions\CreateAction;
use Filament\Resources\Pages\ListRecords;

class ListSOSRequests extends ListRecords
{
    protected static string $resource = SOSRequestResource::class;

    protected function getHeaderActions(): array
    {
        return [
            CreateAction::make(),
        ];
    }
}
