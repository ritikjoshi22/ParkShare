<?php

namespace App\Filament\Resources\SOSRequests\Tables;

use App\Models\SOSRequest;
use App\Models\Technician;
use Filament\Actions\Action;
use Filament\Actions\BulkActionGroup;
use Filament\Actions\DeleteBulkAction;
use Filament\Actions\EditAction;
use Filament\Actions\ViewAction;
use Filament\Forms\Components\Select;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Filters\SelectFilter;
use Filament\Tables\Table;
use Illuminate\Support\Carbon;

class SOSRequestsTable
{
    public static function configure(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('user.full_name')
                    ->label('Driver')
                    ->searchable()
                    ->sortable(),
                TextColumn::make('emergency_message')
                    ->limit(50)
                    ->toggleable(),
                TextColumn::make('status')
                    ->badge()
                    ->color(fn (string $state): string => match ($state) {
                        'active' => 'danger',
                        'resolved' => 'success',
                        default => 'gray',
                    }),
                TextColumn::make('technician.user.full_name')
                    ->label('Technician')
                    ->placeholder('Unassigned'),
                TextColumn::make('created_at')
                    ->dateTime()
                    ->sortable(),
                TextColumn::make('resolved_at')
                    ->dateTime()
                    ->sortable()
                    ->toggleable(isToggledHiddenByDefault: true),
            ])
            ->defaultSort('created_at', 'desc')
            ->filters([
                SelectFilter::make('status')
                    ->options([
                        'active' => 'Active',
                        'resolved' => 'Resolved',
                        'cancelled' => 'Cancelled',
                    ]),
            ])
            ->recordActions([
                ViewAction::make(),
                EditAction::make(),
                Action::make('assignTechnician')
                    ->label('Assign')
                    ->icon('heroicon-o-user-plus')
                    ->color('warning')
                    ->visible(fn (SOSRequest $record): bool => $record->status === 'active')
                    ->form([
                        Select::make('technician_id')
                            ->label('Technician')
                            ->options(fn () => Technician::query()
                                ->with('user')
                                ->where('availability_status', 'available')
                                ->get()
                                ->mapWithKeys(fn (Technician $t) => [$t->id => $t->user?->full_name ?? 'Technician #' . $t->id]))
                            ->required()
                            ->searchable(),
                    ])
                    ->action(function (SOSRequest $record, array $data): void {
                        $record->update(['technician_id' => $data['technician_id']]);
                    }),
                Action::make('resolve')
                    ->label('Resolve')
                    ->icon('heroicon-o-check-circle')
                    ->color('success')
                    ->requiresConfirmation()
                    ->visible(fn (SOSRequest $record): bool => $record->status === 'active')
                    ->action(fn (SOSRequest $record) => $record->update([
                        'status' => 'resolved',
                        'resolved_at' => Carbon::now(),
                    ])),
            ])
            ->toolbarActions([
                BulkActionGroup::make([
                    DeleteBulkAction::make(),
                ]),
            ]);
    }
}
