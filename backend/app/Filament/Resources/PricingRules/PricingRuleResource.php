<?php

namespace App\Filament\Resources\PricingRules;

use App\Filament\Resources\PricingRules\Pages\ManagePricingRules;
use App\Models\PricingRule;
use Filament\Forms\Components\Select;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Components\Toggle;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Columns\TextColumn;
use Filament\Tables\Columns\ToggleColumn;
use Filament\Tables\Table;

class PricingRuleResource extends Resource
{
    protected static ?string $model = PricingRule::class;

    protected static ?string $navigationLabel = 'Pricing Rules';

    protected static string|\BackedEnum|null $navigationIcon = Heroicon::OutlinedCurrencyDollar;

    protected static ?int $navigationSort = 11;

    public static function form(Schema $schema): Schema
    {
        return $schema->components([
            Select::make('parking_space_id')
                ->relationship('parkingSpace', 'parking_name')
                ->label('Parking space (empty = global)')
                ->searchable(),
            TextInput::make('hour_from')->numeric()->required()->default(1),
            TextInput::make('hour_to')->numeric()->label('Hour to (empty = unlimited)'),
            TextInput::make('rate')->numeric()->required()->prefix('NPR'),
            TextInput::make('daily_cap')->numeric()->prefix('NPR'),
            TextInput::make('sort_order')->numeric()->default(0),
            Toggle::make('is_active')->default(true),
        ]);
    }

    public static function table(Table $table): Table
    {
        return $table
            ->columns([
                TextColumn::make('parkingSpace.parking_name')->label('Parking')->default('Global'),
                TextColumn::make('hour_from'),
                TextColumn::make('hour_to')->default('∞'),
                TextColumn::make('rate')->money('NPR'),
                TextColumn::make('daily_cap')->money('NPR'),
                ToggleColumn::make('is_active'),
            ])
            ->defaultSort('sort_order');
    }

    public static function getPages(): array
    {
        return [
            'index' => ManagePricingRules::route('/'),
        ];
    }
}
