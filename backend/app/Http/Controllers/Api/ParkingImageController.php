<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Parking\StoreParkingImageRequest;
use App\Http\Resources\ParkingImageResource;
use App\Models\ParkingImage;
use App\Models\ParkingSpace;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class ParkingImageController extends ApiController
{
    public function store(StoreParkingImageRequest $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('manageImages', $parkingSpace);
        $this->authorize('create', ParkingImage::class);

        $image = $this->persistImage($request, $parkingSpace);

        return $this->success(new ParkingImageResource($image), 'Image uploaded.', 201);
    }

    public function storeBatch(Request $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('manageImages', $parkingSpace);
        $this->authorize('create', ParkingImage::class);

        $request->validate([
            'images' => ['required', 'array', 'min:1', 'max:10'],
            'images.*' => ['image', 'mimes:jpeg,jpg,png,webp', 'max:5120'],
        ]);

        $uploaded = [];
        $sort = (int) $parkingSpace->images()->max('sort_order');

        foreach ($request->file('images') as $file) {
            $path = $file->store('parking-images', 'public');
            $sort++;
            $uploaded[] = $parkingSpace->images()->create([
                'image_url' => Storage::disk('public')->url($path),
                'sort_order' => $sort,
                'is_primary' => $parkingSpace->images()->count() === 0,
            ]);
        }

        return $this->success(
            ParkingImageResource::collection(collect($uploaded)),
            count($uploaded).' image(s) uploaded.',
            201
        );
    }

    public function setPrimary(ParkingImage $parkingImage): JsonResponse
    {
        $this->authorize('delete', $parkingImage);

        $parkingImage->parkingSpace->images()->update(['is_primary' => false]);
        $parkingImage->update(['is_primary' => true]);

        return $this->success(new ParkingImageResource($parkingImage), 'Primary image updated.');
    }

    public function destroy(ParkingImage $parkingImage): JsonResponse
    {
        $this->authorize('delete', $parkingImage);

        if (str_contains($parkingImage->image_url, '/storage/')) {
            $relative = str_replace(Storage::disk('public')->url(''), '', $parkingImage->image_url);
            Storage::disk('public')->delete(ltrim($relative, '/'));
        }

        $wasPrimary = $parkingImage->is_primary;
        $spaceId = $parkingImage->parking_space_id;
        $parkingImage->delete();

        if ($wasPrimary) {
            $next = ParkingImage::where('parking_space_id', $spaceId)->orderBy('sort_order')->first();
            $next?->update(['is_primary' => true]);
        }

        return $this->success(null, 'Image deleted.');
    }

    protected function persistImage(StoreParkingImageRequest $request, ParkingSpace $parkingSpace): ParkingImage
    {
        $imageUrl = $request->input('image_url');

        if ($request->hasFile('image')) {
            $path = $request->file('image')->store('parking-images', 'public');
            $imageUrl = Storage::disk('public')->url($path);
        }

        $sort = (int) $parkingSpace->images()->max('sort_order') + 1;

        return $parkingSpace->images()->create([
            'image_url' => $imageUrl,
            'sort_order' => $sort,
            'is_primary' => ! $parkingSpace->images()->exists(),
        ]);
    }
}
