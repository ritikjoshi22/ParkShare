<?php

namespace App\Http\Requests\Review;

use Illuminate\Foundation\Http\FormRequest;

class StoreReviewRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'parking_space_id' => ['nullable', 'integer', 'exists:parking_spaces,id', 'required_without:technician_id'],
            'technician_id' => ['nullable', 'integer', 'exists:technicians,id', 'required_without:parking_space_id'],
            'rating' => ['required', 'integer', 'between:1,5'],
            'review_text' => ['nullable', 'string', 'max:2000'],
        ];
    }

    protected function prepareForValidation(): void
    {
        if ($this->filled('review_text')) {
            $this->merge(['review_text' => strip_tags((string) $this->input('review_text'))]);
        }
    }
}
