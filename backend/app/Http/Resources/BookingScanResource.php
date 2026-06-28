<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class BookingScanResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'action' => $this->resource['action'],
            'message' => $this->resource['message'],
            'payment_required' => $this->resource['payment_required'] ?? false,
            'amount_due' => $this->resource['amount_due'] ?? 0,
            'booking' => new BookingResource($this->resource['booking']),
        ];
    }
}
