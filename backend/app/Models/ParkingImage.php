<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ParkingImage extends Model
{
    protected $fillable = [
        'parking_space_id',
        'image_url'
    ];

    public function parkingSpace()
    {
        return $this->belongsTo(ParkingSpace::class);
    }
}
