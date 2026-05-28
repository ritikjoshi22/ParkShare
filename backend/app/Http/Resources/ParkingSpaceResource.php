<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ParkingSpaceResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'owner_id' => $this->owner_id,
            'parking_name' => $this->parking_name,
            'description' => $this->description,
            'address' => $this->address,
            'latitude' => $this->latitude,
            'longitude' => $this->longitude,
            'price_per_hour' => $this->price_per_hour,
            'total_slots' => $this->total_slots,
            'available_slots' => $this->available_slots,
            'vehicle_type' => $this->vehicle_type,
            'opening_time' => $this->opening_time,
            'closing_time' => $this->closing_time,
            'is_verified' => $this->is_verified,
            'is_active' => $this->is_active,
            'distance_km' => $this->when(isset($this->distance_km), round((float) $this->distance_km, 2)),
            'owner' => UserResource::make($this->whenLoaded('owner')),
            'images' => ParkingImageResource::collection($this->whenLoaded('images')),
            'availability' => ParkingAvailabilityResource::collection($this->whenLoaded('availability')),
            'reviews_avg_rating' => $this->when(isset($this->reviews_avg_rating), $this->reviews_avg_rating),
            'created_at' => $this->created_at?->toIso8601String(),
        ];
    }
}
