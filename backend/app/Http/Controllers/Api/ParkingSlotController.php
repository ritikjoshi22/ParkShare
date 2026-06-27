<?php

namespace App\Http\Controllers\Api;

use App\Http\Resources\ParkingSlotResource;
use App\Models\Booking;
use App\Models\ParkingSpace;
use App\Models\ParkingSlot;
use Carbon\Carbon;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ParkingSlotController extends ApiController
{
    public function index(Request $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('view', $parkingSpace);

        $start = $request->filled('start_time')
            ? Carbon::parse($request->input('start_time'))
            : Carbon::now();
        $end = $request->filled('end_time')
            ? Carbon::parse($request->input('end_time'))
            : $start->copy()->addHours(2);

        $slots = $parkingSpace->slots()->orderBy('sort_order')->get();

        $bookedSlotIds = Booking::forParking($parkingSpace->id)
            ->whereNotIn('booking_status', ['cancelled', 'completed'])
            ->where('start_time', '<', $end)
            ->where('end_time', '>', $start)
            ->whereNotNull('parking_slot_id')
            ->pluck('parking_slot_id')
            ->all();

        $slots->each(function (ParkingSlot $slot) use ($bookedSlotIds) {
            if ($slot->status === 'maintenance') {
                $slot->display_status = 'maintenance';
            } elseif (in_array($slot->id, $bookedSlotIds, true)) {
                $slot->display_status = 'occupied';
            } else {
                $slot->display_status = 'available';
            }
        });

        return $this->success(ParkingSlotResource::collection($slots));
    }
}
