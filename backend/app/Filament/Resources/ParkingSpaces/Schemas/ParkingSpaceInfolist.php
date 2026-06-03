<?php

namespace App\Filament\Resources\ParkingSpaces\Schemas;

use Filament\Infolists\Components\IconEntry;
use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Schema;

class ParkingSpaceInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                TextEntry::make('owner_id')
                    ->numeric(),
                TextEntry::make('parking_name'),
                TextEntry::make('description')
                    ->placeholder('-')
                    ->columnSpanFull(),
                TextEntry::make('address')
                    ->columnSpanFull(),
                TextEntry::make('latitude')
                    ->numeric(),
                TextEntry::make('longitude')
                    ->numeric(),
                TextEntry::make('price_per_hour')
                    ->numeric(),
                TextEntry::make('total_slots')
                    ->numeric(),
                TextEntry::make('available_slots')
                    ->numeric(),
                TextEntry::make('vehicle_type'),
                TextEntry::make('opening_time')
                    ->time(),
                TextEntry::make('closing_time')
                    ->time(),
                IconEntry::make('is_verified')
                    ->boolean(),
                IconEntry::make('is_active')
                    ->boolean(),
                TextEntry::make('created_at')
                    ->dateTime()
                    ->placeholder('-'),
                TextEntry::make('updated_at')
                    ->dateTime()
                    ->placeholder('-'),
            ]);
    }
}
