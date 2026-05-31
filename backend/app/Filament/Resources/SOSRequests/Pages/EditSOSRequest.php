<?php

namespace App\Filament\Resources\SOSRequests\Pages;

use App\Filament\Resources\SOSRequests\SOSRequestResource;
use Filament\Actions\DeleteAction;
use Filament\Actions\ViewAction;
use Filament\Resources\Pages\EditRecord;

class EditSOSRequest extends EditRecord
{
    protected static string $resource = SOSRequestResource::class;

    protected function getHeaderActions(): array
    {
        return [
            ViewAction::make(),
            DeleteAction::make(),
        ];
    }
}
