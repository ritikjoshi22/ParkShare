<?php

namespace App\Http\Requests\SOS;

use Illuminate\Foundation\Http\FormRequest;

class StoreSOSRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'latitude' => ['required', 'numeric', 'between:-90,90'],
            'longitude' => ['required', 'numeric', 'between:-180,180'],
            'emergency_message' => ['nullable', 'string', 'max:1000'],
        ];
    }

    protected function prepareForValidation(): void
    {
        if ($this->filled('emergency_message')) {
            $this->merge(['emergency_message' => strip_tags((string) $this->input('emergency_message'))]);
        }
    }
}
