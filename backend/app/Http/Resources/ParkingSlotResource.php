<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class ParkingSlotResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'parking_space_id' => $this->parking_space_id,
            'slot_number' => $this->slot_number,
            'label' => $this->label,
            'status' => $this->status,
            'sort_order' => $this->sort_order,
            'display_status' => $this->when(isset($this->display_status), $this->display_status),
        ];
    }
}
