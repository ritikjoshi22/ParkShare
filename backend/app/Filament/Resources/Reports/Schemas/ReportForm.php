<?php

namespace App\Filament\Resources\Reports\Schemas;

use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Textarea;
use Filament\Schemas\Schema;

class ReportForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                TextInput::make('reported_by')
                    ->required()
                    ->numeric(),
                TextInput::make('booking_id')
                    ->numeric(),
                TextInput::make('parking_space_id')
                    ->numeric(),
                Textarea::make('report_reason')
                    ->required()
                    ->columnSpanFull(),
                TextInput::make('status')
                    ->required()
                    ->default('pending'),
            ]);
    }
}
