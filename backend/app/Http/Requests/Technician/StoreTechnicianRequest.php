<?php

namespace App\Http\Requests\Technician;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StoreTechnicianRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'specialization' => ['required', 'string', 'max:255'],
            'experience_years' => ['required', 'integer', 'min:0'],
            'service_radius_km' => ['required', 'integer', 'min:1', 'max:500'],
            'availability_status' => ['sometimes', Rule::in(['available', 'busy', 'offline'])],
            'description' => ['nullable', 'string', 'max:2000'],
            'hourly_rate' => ['nullable', 'numeric', 'min:0'],
        ];
    }
}
