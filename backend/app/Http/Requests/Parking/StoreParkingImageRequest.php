<?php

namespace App\Http\Requests\Parking;

use Illuminate\Foundation\Http\FormRequest;

class StoreParkingImageRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'image' => ['required_without:image_url', 'image', 'max:5120'],
            'image_url' => ['required_without:image', 'string', 'max:500'],
        ];
    }
}
