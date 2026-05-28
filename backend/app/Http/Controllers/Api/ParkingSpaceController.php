<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Parking\StoreParkingSpaceRequest;
use App\Http\Requests\Parking\UpdateParkingSpaceRequest;
use App\Http\Resources\ParkingSpaceResource;
use App\Models\ParkingSpace;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ParkingSpaceController extends ApiController
{
    public function index(Request $request): JsonResponse
    {
        $this->authorize('viewAny', ParkingSpace::class);

        $query = ParkingSpace::query()
            ->with(['images', 'owner'])
            ->withAvg('reviews', 'rating')
            ->active();

        if ($request->user()?->isOwner() && ! $request->user()->isAdmin()) {
            $query->where('owner_id', $request->user()->id);
        } elseif ($request->boolean('verified_only', true) && ! $request->user()?->isAdmin()) {
            $query->verified();
        }

        if ($request->filled('latitude') && $request->filled('longitude')) {
            $query->nearby(
                (float) $request->latitude,
                (float) $request->longitude,
                (float) $request->input('radius_km', 10)
            );
        } else {
            $query->latest();
        }

        if ($request->vehicle_type) {
            $query->whereIn('vehicle_type', [$request->vehicle_type, 'both']);
        }

        $spaces = $query->paginate($request->integer('per_page', 15));

        return $this->success(ParkingSpaceResource::collection($spaces));
    }

    public function store(StoreParkingSpaceRequest $request): JsonResponse
    {
        $this->authorize('create', ParkingSpace::class);

        $space = ParkingSpace::create([
            ...$request->validated(),
            'owner_id' => $request->user()->id,
            'is_verified' => $request->user()->isAdmin(),
        ]);

        return $this->success(
            new ParkingSpaceResource($space->load('images', 'owner')),
            'Parking space created.',
            201
        );
    }

    public function show(ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('view', $parkingSpace);

        $parkingSpace->load(['images', 'owner', 'availability', 'reviews.user'])
            ->loadAvg('reviews', 'rating');

        return $this->success(new ParkingSpaceResource($parkingSpace));
    }

    public function update(UpdateParkingSpaceRequest $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('update', $parkingSpace);

        $parkingSpace->update($request->validated());

        return $this->success(
            new ParkingSpaceResource($parkingSpace->fresh()->load('images', 'owner')),
            'Parking space updated.'
        );
    }

    public function destroy(ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('delete', $parkingSpace);

        $parkingSpace->delete();

        return $this->success(null, 'Parking space deleted.');
    }

    public function nearby(Request $request): JsonResponse
    {
        $request->validate([
            'latitude' => ['required', 'numeric', 'between:-90,90'],
            'longitude' => ['required', 'numeric', 'between:-180,180'],
            'radius_km' => ['nullable', 'numeric', 'min:0.1', 'max:100'],
        ]);

        $spaces = ParkingSpace::query()
            ->active()
            ->verified()
            ->with(['images'])
            ->withAvg('reviews', 'rating')
            ->nearby(
                (float) $request->latitude,
                (float) $request->longitude,
                (float) $request->input('radius_km', 10)
            )
            ->paginate($request->integer('per_page', 15));

        return $this->success(ParkingSpaceResource::collection($spaces));
    }
}
