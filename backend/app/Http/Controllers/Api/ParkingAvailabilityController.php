<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Parking\StoreParkingAvailabilityRequest;
use App\Http\Resources\ParkingAvailabilityResource;
use App\Models\ParkingAvailability;
use App\Models\ParkingSpace;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ParkingAvailabilityController extends ApiController
{
    public function index(Request $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('view', $parkingSpace);

        $slots = $parkingSpace->availability()
            ->when($request->date, fn ($q, $date) => $q->where('available_date', $date))
            ->orderBy('available_date')
            ->paginate($request->integer('per_page', 15));

        return $this->success(ParkingAvailabilityResource::collection($slots));
    }

    public function store(StoreParkingAvailabilityRequest $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('update', $parkingSpace);
        $this->authorize('create', ParkingAvailability::class);

        $slot = $parkingSpace->availability()->create($request->validated());

        return $this->success(new ParkingAvailabilityResource($slot), 'Availability slot created.', 201);
    }

    public function destroy(ParkingAvailability $parkingAvailability): JsonResponse
    {
        $this->authorize('delete', $parkingAvailability);

        $parkingAvailability->delete();

        return $this->success(null, 'Availability slot deleted.');
    }
}
