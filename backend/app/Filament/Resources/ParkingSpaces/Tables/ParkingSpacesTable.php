<?php

namespace App\Filament\Resources\ParkingSpaces\Tables;

use App\Models\ParkingSpace;
use Filament\Actions\Action;
use Filament\Actions\BulkActionGroup;
use Filament\Actions\DeleteBulkAction;
use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Tables\Columns\IconColumn;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Filters\TernaryFilter;
use Filament\Tables\Table;

class ParkingSpacesTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('parking_name')
                    ->searchable()
                    ->sortable(),
                TextColumn::make('owner.full_name')
                    ->label('Owner')
                    ->searchable()
                    ->sortable(),
                TextColumn::make('address')
                    ->limit(40)
                    ->searchable()
                    ->toggleable(),
                TextColumn::make('price_per_hour')
                    ->label('NPR/hr')
                    ->money('NPR')
                    ->sortable(),
                TextColumn::make('available_slots')
                    ->label('Available')
                    ->numeric()
                    ->sortable(),
                TextColumn::make('total_slots')
                    ->numeric()
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
                IconColumn::make('is_verified')
                    ->label('Verified')
                    ->boolean(),
                IconColumn::make('is_active')
                    ->label('Active')
                    ->boolean(),
                TextColumn::make('created_at')
                    ->dateTime()
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
            ])
            ->filters([
                TernaryFilter::make('is_verified')
                    ->label('Verification'),
                TernaryFilter::make('is_active')
                    ->label('Active'),
                SelectFilter::make('vehicle_type')
                    ->options([
                        'car' => 'Car',
                        'bike' => 'Bike',
                        'both' => 'Both',
                    ]),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
                Action::make('approve')
                    ->label('Approve')
                    ->icon('heroicon-o-check-badge')
                    ->color('success')
                    ->requiresConfirmation()
                    ->visible(fn (ParkingSpace $record): bool => ! $record->is_verified)
                    ->action(fn (ParkingSpace $record) => $record->update([
                        'is_verified' => true,
                        'is_active' => true,
                    ])),
                Action::make('reject')
                    ->label('Reject')
                    ->icon('heroicon-o-x-circle')
                    ->color('danger')
                    ->requiresConfirmation()
                    ->visible(fn (ParkingSpace $record): bool => $record->is_verified || $record->is_active)
                    ->action(fn (ParkingSpace $record) => $record->update([
                        'is_verified' => false,
                        'is_active' => false,
                    ])),
            ])
            ->toolbarActions([
                BulkActionGroup::make([
                    DeleteBulkAction::make(),
                ]),
            ]);
    }
}
