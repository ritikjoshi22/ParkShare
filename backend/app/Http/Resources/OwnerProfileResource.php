<?php

namespace App\Http\Resources;

use Illuminate\Http\Request;
use Illuminate\Http\Resources\Json\JsonResource;

class OwnerProfileResource extends JsonResource
{
    public function toArray(Request $request): array
    {
        return [
            'id' => $this->id,
            'status' => $this->status,
            'current_step' => $this->current_step,
            'step_data' => (object) ($this->step_data ?? []),
            'submitted_at' => $this->submitted_at?->toIso8601String(),
            'verified_at' => $this->verified_at?->toIso8601String(),
            'rejected_at' => $this->rejected_at?->toIso8601String(),
            'rejection_reason' => $this->rejection_reason,
            'documents' => OwnerDocumentResource::collection($this->whenLoaded('documents')),
        ];
    }
}
