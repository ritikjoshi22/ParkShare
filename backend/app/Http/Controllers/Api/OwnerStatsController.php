<?php

namespace App\Http\Controllers\Api;

use App\Models\Booking;
use App\Models\ParkingSpace;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class OwnerStatsController extends ApiController
{
    public function index(Request $request): JsonResponse
    {
        if (! $request->user()->isOwner() && ! $request->user()->isAdmin()) {
            return $this->error('Forbidden.', 403);
        }

        $ownerId = $request->user()->id;
        $parkingIds = ParkingSpace::where('owner_id', $ownerId)->pluck('id');

        $totalSpaces = $parkingIds->count();
        $availableSlots = ParkingSpace::whereIn('id', $parkingIds)->sum('available_slots');
        $totalSlots = ParkingSpace::whereIn('id', $parkingIds)->sum('total_slots');

        $activeBookings = Booking::whereIn('parking_space_id', $parkingIds)
            ->whereIn('booking_status', ['pending', 'confirmed', 'checked_in'])
            ->count();

        $monthlyRevenue = Booking::whereIn('parking_space_id', $parkingIds)
            ->whereIn('booking_status', ['confirmed', 'completed'])
            ->whereMonth('created_at', now()->month)
            ->whereYear('created_at', now()->year)
            ->sum('total_amount');

        $topParking = Booking::query()
            ->select('parking_space_id', DB::raw('count(*) as bookings_count'))
            ->whereIn('parking_space_id', $parkingIds)
            ->groupBy('parking_space_id')
            ->orderByDesc('bookings_count')
            ->with('parkingSpace:id,parking_name')
            ->first();

        return $this->success([
            'total_parking_spaces' => $totalSpaces,
            'active_bookings' => $activeBookings,
            'monthly_revenue' => round((float) $monthlyRevenue, 2),
            'available_slots' => (int) $availableSlots,
            'total_slots' => (int) $totalSlots,
            'occupancy_rate' => $totalSlots > 0
                ? round((($totalSlots - $availableSlots) / $totalSlots) * 100, 1)
                : 0,
            'top_parking_name' => $topParking?->parkingSpace?->parking_name,
            'top_parking_bookings' => $topParking?->bookings_count ?? 0,
        ]);
    }
}
