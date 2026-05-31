<?php

namespace App\Filament\Resources\Bookings\Schemas;

use Filament\Infolists\Components\TextEntry;
use Filament\Schemas\Schema;

class BookingInfolist
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                TextEntry::make('user_id')
                    ->numeric(),
                TextEntry::make('parking_space_id')
                    ->numeric(),
                TextEntry::make('booking_date')
                    ->date(),
                TextEntry::make('start_time')
                    ->dateTime(),
                TextEntry::make('end_time')
                    ->dateTime(),
                TextEntry::make('total_hours')
                    ->numeric(),
                TextEntry::make('total_amount')
                    ->numeric(),
                TextEntry::make('booking_status'),
                TextEntry::make('qr_code')
                    ->placeholder('-'),
                TextEntry::make('checked_in_at')
                    ->dateTime()
                    ->placeholder('-'),
                TextEntry::make('checked_out_at')
                    ->dateTime()
                    ->placeholder('-'),
                TextEntry::make('overtime_fee')
                    ->numeric(),
                TextEntry::make('created_at')
                    ->dateTime()
                    ->placeholder('-'),
                TextEntry::make('updated_at')
                    ->dateTime()
                    ->placeholder('-'),
            ]);
    }
}
