<?php

namespace App\Http\Controllers\Api;

use App\Http\Resources\FavoriteParkingResource;
use App\Models\FavoriteParking;
use App\Models\ParkingSpace;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;

class FavoriteParkingController extends ApiController
{
    public function index(Request $request): JsonResponse
    {
        $this->authorize('viewAny', FavoriteParking::class);

        $favorites = $request->user()
            ->favoriteParking()
            ->with(['parkingSpace.images'])
            ->latest()
            ->paginate($request->integer('per_page', 15));

        return $this->success(FavoriteParkingResource::collection($favorites));
    }

    public function store(Request $request): JsonResponse
    {
        $this->authorize('create', FavoriteParking::class);

        $request->validate([
            'parking_space_id' => ['required', 'integer', 'exists:parking_spaces,id'],
        ]);

        $exists = FavoriteParking::where('user_id', $request->user()->id)
            ->where('parking_space_id', $request->parking_space_id)
            ->exists();

        if ($exists) {
            throw ValidationException::withMessages([
                'parking_space_id' => ['This parking is already in your favorites.'],
            ]);
        }

        $favorite = FavoriteParking::create([
            'user_id' => $request->user()->id,
            'parking_space_id' => $request->parking_space_id,
        ]);

        return $this->success(
            new FavoriteParkingResource($favorite->load('parkingSpace.images')),
            'Added to favorites.',
            201
        );
    }

    public function destroy(FavoriteParking $favoriteParking): JsonResponse
    {
        $this->authorize('delete', $favoriteParking);

        $favoriteParking->delete();

        return $this->success(null, 'Removed from favorites.');
    }
}
