<?php

namespace App\Filament\Resources\SOSRequests;

use App\Filament\Resources\SOSRequests\Pages\CreateSOSRequest;
use App\Filament\Resources\SOSRequests\Pages\EditSOSRequest;
use App\Filament\Resources\SOSRequests\Pages\ListSOSRequests;
use App\Filament\Resources\SOSRequests\Pages\ViewSOSRequest;
use App\Filament\Resources\SOSRequests\Schemas\SOSRequestForm;
use App\Filament\Resources\SOSRequests\Schemas\SOSRequestInfolist;
use App\Filament\Resources\SOSRequests\Tables\SOSRequestsTable;
use App\Models\SOSRequest;
use BackedEnum;
use Filament\Resources\Resource;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;
use Filament\Tables\Table;

class SOSRequestResource extends Resource
{
    protected static ?string $model = SOSRequest::class;

    protected static ?string $navigationLabel = 'SOS Requests';

    protected static string|BackedEnum|null $navigationIcon = Heroicon::OutlinedExclamationTriangle;

    protected static ?int $navigationSort = 4;

    public static function form(Schema $schema): Schema
    {
        return SOSRequestForm::configure($schema);
    }

    public static function infolist(Schema $schema): Schema
    {
        return SOSRequestInfolist::configure($schema);
    }

    public static function table(Table $table): Table
    {
        return SOSRequestsTable::configure($table);
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
            'index' => ListSOSRequests::route('/'),
            'create' => CreateSOSRequest::route('/create'),
            'view' => ViewSOSRequest::route('/{record}'),
            'edit' => EditSOSRequest::route('/{record}/edit'),
        ];
    }
}
