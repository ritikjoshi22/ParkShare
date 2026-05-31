<?php

namespace App\Filament\Resources\SOSRequests\Pages;

use App\Filament\Resources\SOSRequests\SOSRequestResource;
use Filament\Actions\EditAction;
use Filament\Resources\Pages\ViewRecord;

class ViewSOSRequest extends ViewRecord
{
    protected static string $resource = SOSRequestResource::class;

    protected function getHeaderActions(): array
    {
        return [
            EditAction::make(),
        ];
    }
}
