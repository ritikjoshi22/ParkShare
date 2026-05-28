<?php

namespace App\Http\Requests\Parking;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StoreParkingSpaceRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'parking_name' => ['required', 'string', 'max:255'],
            'description' => ['nullable', 'string', 'max:2000'],
            'address' => ['required', 'string', 'max:500'],
            'latitude' => ['required', 'numeric', 'between:-90,90'],
            'longitude' => ['required', 'numeric', 'between:-180,180'],
            'price_per_hour' => ['required', 'numeric', 'min:0'],
            'total_slots' => ['required', 'integer', 'min:1'],
            'available_slots' => ['required', 'integer', 'min:0', 'lte:total_slots'],
            'vehicle_type' => ['required', Rule::in(['bike', 'car', 'both'])],
            'opening_time' => ['required', 'date_format:H:i'],
            'closing_time' => ['required', 'date_format:H:i', 'after:opening_time'],
        ];
    }

    protected function prepareForValidation(): void
    {
        $this->merge([
            'parking_name' => strip_tags((string) $this->input('parking_name')),
            'description' => $this->filled('description') ? strip_tags((string) $this->input('description')) : null,
            'address' => strip_tags((string) $this->input('address')),
        ]);
    }
}
