<?php

namespace App\Filament\Resources\ParkingSpaces;

use App\Filament\Resources\ParkingSpaces\Pages\CreateParkingSpace;
use App\Filament\Resources\ParkingSpaces\Pages\EditParkingSpace;
use App\Filament\Resources\ParkingSpaces\Pages\ListParkingSpaces;
use App\Filament\Resources\ParkingSpaces\Pages\ViewParkingSpace;
use App\Filament\Resources\ParkingSpaces\Schemas\ParkingSpaceForm;
use App\Filament\Resources\ParkingSpaces\Schemas\ParkingSpaceInfolist;
use App\Filament\Resources\ParkingSpaces\Tables\ParkingSpacesTable;
use App\Models\ParkingSpace;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Table;

class ParkingSpaceResource extends Resource
{
    protected static ?string $model = ParkingSpace::class;

    protected static ?string $navigationLabel = 'Parking Spaces';

    protected static string|BackedEnum|null $navigationIcon = Heroicon::OutlinedBuildingOffice2;

    protected static ?int $navigationSort = 2;

    public static function form(Schema $schema): Schema
    {
        return ParkingSpaceForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return ParkingSpaceInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return ParkingSpacesTable::configure($table);
    }

    public static function getRelations(): array
    {
        return [
            //
        ];
    }

    public static function getPages(): array
    {
        return [
            'index' => ListParkingSpaces::route('/'),
            'create' => CreateParkingSpace::route('/create'),
            'view' => ViewParkingSpace::route('/{record}'),
            'edit' => EditParkingSpace::route('/{record}/edit'),
        ];
    }
}
