<?php

namespace App\Filament\Pages;

use App\Services\SystemSettingsService;
use Filament\Forms\Components\TextInput;
use Filament\Forms\Concerns\InteractsWithForms;
use Filament\Forms\Contracts\HasForms;
use Filament\Notifications\Notification;
use Filament\Pages\Page;
use Filament\Schemas\Schema;
use Filament\Support\Icons\Heroicon;

class BookingSettings extends Page implements HasForms
{
    use InteractsWithForms;

    protected static string|\BackedEnum|null $navigationIcon = Heroicon::OutlinedCog6Tooth;

    protected static ?string $navigationLabel = 'Booking Settings';

    protected static ?string $title = 'Booking Engine Settings';

    protected static ?int $navigationSort = 10;

    protected string $view = 'filament.pages.booking-settings';

    public ?array $data = [];

    public function mount(SystemSettingsService $settings): void
    {
        $this->form->fill($settings->all());
    }

    public function form(Schema $schema): Schema
    {
        return $schema
            ->components([
                TextInput::make('early_check_in_buffer_minutes')
                    ->label('Early check-in buffer (minutes)')
                    ->numeric()->required(),
                TextInput::make('late_check_in_grace_minutes')
                    ->label('Late check-in grace (minutes)')
                    ->numeric()->required(),
                TextInput::make('exit_grace_minutes')
                    ->label('Exit grace (minutes)')
                    ->numeric()->required(),
                TextInput::make('booking_buffer_minutes')
                    ->label('Buffer between bookings (minutes)')
                    ->numeric()->required(),
                TextInput::make('fine_per_15_minutes')
                    ->label('Overstay fine per 15 min (NPR)')
                    ->numeric()->required(),
                TextInput::make('extension_reminder_minutes')
                    ->label('Extension reminder before end (minutes)')
                    ->numeric()->required(),
            ])
            ->statePath('data');
    }

    public function save(SystemSettingsService $settings): void
    {
        $data = $this->form->getState();

        foreach ($data as $key => $value) {
            $settings->set($key, is_numeric($value) ? (float) $value : $value);
        }

        Notification::make()->title('Settings saved')->success()->send();
    }
}
