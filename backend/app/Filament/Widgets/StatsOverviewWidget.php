<?php

namespace App\Filament\Widgets;

use App\Models\Booking;
use App\Models\ParkingSpace;
use App\Models\SOSRequest;
use App\Models\Technician;
use App\Models\User;
use Filament\Widgets\StatsOverviewWidget as BaseStatsOverviewWidget;
use Filament\Widgets\StatsOverviewWidget\Stat;

class StatsOverviewWidget extends BaseStatsOverviewWidget
{
    protected function getStats(): array
    {
        $totalRevenue = Booking::query()
            ->whereIn('booking_status', ['confirmed', 'completed'])
            ->sum('total_amount');

        $activeBookings = Booking::query()
            ->whereIn('booking_status', ['pending', 'confirmed'])
            ->count();

        return [
            Stat::make('Total Users', User::count())
                ->description(User::where('is_active', true)->count() . ' active')
                ->descriptionIcon('heroicon-m-users')
                ->color('primary'),
            Stat::make('Parking Spaces', ParkingSpace::count())
                ->description(ParkingSpace::where('is_verified', true)->count() . ' verified')
                ->descriptionIcon('heroicon-m-building-office-2')
                ->color('success'),
            Stat::make('Active Bookings', $activeBookings)
                ->description('Pending + confirmed')
                ->descriptionIcon('heroicon-m-calendar-days')
                ->color('warning'),
            Stat::make('SOS Requests', SOSRequest::where('status', 'active')->count())
                ->description(SOSRequest::where('status', 'resolved')->count() . ' resolved')
                ->descriptionIcon('heroicon-m-exclamation-triangle')
                ->color('danger'),
            Stat::make('Revenue (NPR)', number_format($totalRevenue, 0))
                ->description('Confirmed & completed bookings')
                ->descriptionIcon('heroicon-m-banknotes')
                ->color('success'),
            Stat::make('Technicians Online', Technician::where('availability_status', 'available')->count())
                ->description(Technician::count() . ' total')
                ->descriptionIcon('heroicon-m-wrench-screwdriver')
                ->color('info'),
        ];
    }
}
