<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ParkingAvailability extends Model
{
    protected $table = 'parking_availability';

    protected $fillable = [
        'parking_space_id',
        'available_date',
        'start_time',
        'end_time',
        'is_available'
    ];

    public function parkingSpace()
    {
        return $this->belongsTo(ParkingSpace::class);
    }
}
