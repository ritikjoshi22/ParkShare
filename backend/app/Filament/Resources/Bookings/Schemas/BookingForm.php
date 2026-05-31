<?php

namespace App\Filament\Resources\Bookings\Schemas;

use Filament\Forms\Components\DatePicker;
use Filament\Forms\Components\DateTimePicker;
use Filament\Forms\Components\TextInput;
use Filament\Schemas\Schema;

class BookingForm
{
    public static function configure(Schema $schema): Schema
    {
        return $schema
            ->components([
                TextInput::make('user_id')
                    ->required()
                    ->numeric(),
                TextInput::make('parking_space_id')
                    ->required()
                    ->numeric(),
                DatePicker::make('booking_date')
                    ->required(),
                DateTimePicker::make('start_time')
                    ->required(),
                DateTimePicker::make('end_time')
                    ->required(),
                TextInput::make('total_hours')
                    ->required()
                    ->numeric(),
                TextInput::make('total_amount')
                    ->required()
                    ->numeric(),
                TextInput::make('booking_status')
                    ->required()
                    ->default('pending'),
                TextInput::make('qr_code'),
                DateTimePicker::make('checked_in_at'),
                DateTimePicker::make('checked_out_at'),
                TextInput::make('overtime_fee')
                    ->required()
                    ->numeric()
                    ->default(0),
            ]);
    }
}
