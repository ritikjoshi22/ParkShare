<?php

namespace App\Filament\Widgets;

use App\Enums\UserRole;
use App\Models\Booking;
use App\Models\ParkingSpace;
use App\Models\Report;
use App\Models\Review;
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

        return [
            Stat::make('Total Users', User::count())
                ->description(
                    User::where('is_active', true)->count().' active · '.
                    User::where('role', UserRole::Driver->value)->count().' drivers'
                )
                ->descriptionIcon('heroicon-m-users')
                ->color('primary'),
            Stat::make('Owners', User::where('role', UserRole::Owner->value)->count())
                ->description(User::where('role', UserRole::Technician->value)->count().' technicians')
                ->descriptionIcon('heroicon-m-building-storefront')
                ->color('info'),
            Stat::make('Parking Spaces', ParkingSpace::count())
                ->description(ParkingSpace::where('is_verified', true)->count().' verified · '.
                    ParkingSpace::where('is_verified', false)->count().' pending')
                ->descriptionIcon('heroicon-m-building-office-2')
                ->color('success'),
            Stat::make('Active Bookings', Booking::whereIn('booking_status', ['pending', 'confirmed'])->count())
                ->description(Booking::where('booking_status', 'completed')->count().' completed')
                ->descriptionIcon('heroicon-m-calendar-days')
                ->color('warning'),
            Stat::make('SOS Active', SOSRequest::where('status', 'active')->count())
                ->description(SOSRequest::where('status', 'resolved')->count().' resolved')
                ->descriptionIcon('heroicon-m-exclamation-triangle')
                ->color('danger'),
            Stat::make('Revenue (NPR)', number_format($totalRevenue, 0))
                ->description('Confirmed & completed')
                ->descriptionIcon('heroicon-m-banknotes')
                ->color('success'),
            Stat::make('Reviews', Review::count())
                ->description(Report::where('status', 'pending')->count().' pending reports')
                ->descriptionIcon('heroicon-m-star')
                ->color('gray'),
            Stat::make('Tech Online', Technician::where('availability_status', 'available')->count())
                ->description(Technician::count().' total')
                ->descriptionIcon('heroicon-m-wrench-screwdriver')
                ->color('info'),
        ];
    }
}
