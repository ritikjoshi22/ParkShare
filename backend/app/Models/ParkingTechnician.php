<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ParkingTechnician extends Model
{
    protected $fillable = [
        'parking_space_id',
        'name',
        'phone',
        'alternate_phone',
        'email',
        'specialization',
        'description',
        'availability_status',
        'is_primary',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'is_primary' => 'boolean',
            'is_active' => 'boolean',
        ];
    }

    public function parkingSpace()
    {
        return $this->belongsTo(ParkingSpace::class);
    }
}
