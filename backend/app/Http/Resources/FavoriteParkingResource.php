<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class FavoriteParkingResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'parking_space_id' => $this->parking_space_id,
            'parking_space' => ParkingSpaceResource::make($this->whenLoaded('parkingSpace')),
            'created_at' => $this->created_at?->toIso8601String(),
        ];
    }
}
