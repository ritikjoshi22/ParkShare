<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ParkingSlot extends Model
{
    protected $fillable = [
        'parking_space_id',
        'slot_number',
        'label',
        'status',
        'sort_order',
    ];

    public function parkingSpace()
    {
        return $this->belongsTo(ParkingSpace::class);
    }

    public function bookings()
    {
        return $this->hasMany(Booking::class);
    }
}
