<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class BookingResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'user_id' => $this->user_id,
            'parking_space_id' => $this->parking_space_id,
            'booking_date' => $this->booking_date?->toDateString(),
            'start_time' => $this->start_time?->toIso8601String(),
            'end_time' => $this->end_time?->toIso8601String(),
            'total_hours' => $this->total_hours,
            'total_amount' => $this->total_amount,
            'booking_status' => $this->booking_status,
            'qr_code' => $this->qr_code,
            'checked_in_at' => $this->checked_in_at?->toIso8601String(),
            'checked_out_at' => $this->checked_out_at?->toIso8601String(),
            'overtime_fee' => $this->overtime_fee,
            'user' => UserResource::make($this->whenLoaded('user')),
            'parking_space' => ParkingSpaceResource::make($this->whenLoaded('parkingSpace')),
            'created_at' => $this->created_at?->toIso8601String(),
        ];
    }
}
