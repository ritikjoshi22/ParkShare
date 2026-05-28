<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class SOSRequestResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'user_id' => $this->user_id,
            'latitude' => $this->latitude,
            'longitude' => $this->longitude,
            'emergency_message' => $this->emergency_message,
            'status' => $this->status,
            'technician_id' => $this->technician_id,
            'resolved_at' => $this->resolved_at?->toIso8601String(),
            'user' => UserResource::make($this->whenLoaded('user')),
            'technician' => TechnicianResource::make($this->whenLoaded('technician')),
            'created_at' => $this->created_at?->toIso8601String(),
        ];
    }
}
