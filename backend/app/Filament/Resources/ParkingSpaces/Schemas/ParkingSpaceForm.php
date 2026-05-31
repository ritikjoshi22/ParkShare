<?php

namespace App\Filament\Resources\ParkingSpaces\Schemas;

use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Textarea;
use Filament\Forms\Components\TimePicker;
use Filament\Forms\Components\Toggle;
use Filament\Schemas\Schema;

class ParkingSpaceForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                TextInput::make('owner_id')
                    ->required()
                    ->numeric(),
                TextInput::make('parking_name')
                    ->required(),
                Textarea::make('description')
                    ->columnSpanFull(),
                Textarea::make('address')
                    ->required()
                    ->columnSpanFull(),
                TextInput::make('latitude')
                    ->required()
                    ->numeric(),
                TextInput::make('longitude')
                    ->required()
                    ->numeric(),
                TextInput::make('price_per_hour')
                    ->required()
                    ->numeric(),
                TextInput::make('total_slots')
                    ->required()
                    ->numeric(),
                TextInput::make('available_slots')
                    ->required()
                    ->numeric(),
                TextInput::make('vehicle_type')
                    ->required()
                    ->default('car'),
                TimePicker::make('opening_time')
                    ->required(),
                TimePicker::make('closing_time')
                    ->required(),
                Toggle::make('is_verified')
                    ->required(),
                Toggle::make('is_active')
                    ->required(),
            ]);
    }
}
