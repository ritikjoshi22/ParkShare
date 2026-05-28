<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Parking\StoreParkingImageRequest;
use App\Http\Resources\ParkingImageResource;
use App\Models\ParkingImage;
use App\Models\ParkingSpace;
use Illuminate\Http\JsonResponse;
use Illuminate\Support\Facades\Storage;

class ParkingImageController extends ApiController
{
    public function store(StoreParkingImageRequest $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('manageImages', $parkingSpace);
        $this->authorize('create', ParkingImage::class);

        $imageUrl = $request->input('image_url');

        if ($request->hasFile('image')) {
            $path = $request->file('image')->store('parking-images', 'public');
            $imageUrl = Storage::disk('public')->url($path);
        }

        $image = $parkingSpace->images()->create(['image_url' => $imageUrl]);

        return $this->success(new ParkingImageResource($image), 'Image uploaded.', 201);
    }

    public function destroy(ParkingImage $parkingImage): JsonResponse
    {
        $this->authorize('delete', $parkingImage);

        $parkingImage->delete();

        return $this->success(null, 'Image deleted.');
    }
}
