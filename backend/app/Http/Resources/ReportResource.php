<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ReportResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'reported_by' => $this->reported_by,
            'booking_id' => $this->booking_id,
            'parking_space_id' => $this->parking_space_id,
            'report_reason' => $this->report_reason,
            'status' => $this->status,
            'reporter' => UserResource::make($this->whenLoaded('reporter')),
            'booking' => BookingResource::make($this->whenLoaded('booking')),
            'parking_space' => ParkingSpaceResource::make($this->whenLoaded('parkingSpace')),
            'created_at' => $this->created_at?->toIso8601String(),
        ];
    }
}
