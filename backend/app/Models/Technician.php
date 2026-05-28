<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class Technician extends Model
{
    protected $fillable = [
        'user_id',
        'specialization',
        'experience_years',
        'service_radius_km',
        'availability_status',
        'description',
        'hourly_rate',
    ];

    protected function casts(): array
    {
        return [
            'hourly_rate' => 'decimal:2',
        ];
    }

    public function scopeAvailable(Builder $query): Builder
    {
        return $query->where('availability_status', 'available');
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function reviews()
    {
        return $this->hasMany(Review::class);
    }

    public function services()
    {
        return $this->hasMany(TechnicianService::class);
    }

    public function sosRequests()
    {
        return $this->hasMany(SOSRequest::class);
    }
}
