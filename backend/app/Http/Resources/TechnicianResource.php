<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class TechnicianResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'user_id' => $this->user_id,
            'specialization' => $this->specialization,
            'experience_years' => $this->experience_years,
            'service_radius_km' => $this->service_radius_km,
            'availability_status' => $this->availability_status,
            'description' => $this->description,
            'hourly_rate' => $this->hourly_rate,
            'user' => UserResource::make($this->whenLoaded('user')),
            'services' => TechnicianServiceResource::collection($this->whenLoaded('services')),
            'reviews_avg_rating' => $this->when(isset($this->reviews_avg_rating), $this->reviews_avg_rating),
        ];
    }
}
