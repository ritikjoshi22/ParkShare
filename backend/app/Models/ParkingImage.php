<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ParkingImage extends Model
{
    protected $fillable = [
        'parking_space_id',
        'image_url',
        'is_primary',
        'sort_order',
    ];

    protected function casts(): array
    {
        return [
            'is_primary' => 'boolean',
        ];
    }

    public function parkingSpace()
    {
        return $this->belongsTo(ParkingSpace::class);
    }
}
