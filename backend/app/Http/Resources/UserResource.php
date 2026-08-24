<?php

namespace App\Http\Resources;

use App\Services\OwnerVerificationService;
use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class UserResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        $verificationService = app(OwnerVerificationService::class);
        $status = $verificationService->statusPayload($this->resource);

        return [
            'id' => $this->id,
            'full_name' => $this->full_name,
            'email' => $this->email,
            'phone' => $this->phone,
            'role' => $this->role,
            'profile_image' => $this->profile_image,
            'address' => $this->address,
            'latitude' => $this->latitude,
            'longitude' => $this->longitude,
            'is_active' => $this->is_active,
            'created_at' => $this->created_at?->toIso8601String(),
            'capabilities' => $status['capabilities'],
            'owner' => $status['owner'],
            'technician' => TechnicianResource::make($this->whenLoaded('technician')),
        ];
    }
}
