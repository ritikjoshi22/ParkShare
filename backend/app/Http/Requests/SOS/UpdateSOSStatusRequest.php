<?php

namespace App\Http\Requests\SOS;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateSOSStatusRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'status' => ['required', Rule::in(['active', 'resolved', 'cancelled'])],
            'technician_id' => ['nullable', 'integer', 'exists:technicians,id'],
        ];
    }
}
