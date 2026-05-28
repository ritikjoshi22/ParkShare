<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class FavoriteParking extends Model
{
     protected $table = 'favorite_parking';

    protected $fillable = [
        'user_id',
        'parking_space_id'
    ];

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function parkingSpace()
    {
        return $this->belongsTo(ParkingSpace::class);
    }
}
