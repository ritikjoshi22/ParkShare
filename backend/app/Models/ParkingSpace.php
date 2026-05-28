<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class ParkingSpace extends Model
{
    protected $fillable = [
        'owner_id',
        'parking_name',
        'description',
        'address',
        'latitude',
        'longitude',
        'price_per_hour',
        'total_slots',
        'available_slots',
        'vehicle_type',
        'opening_time',
        'closing_time',
        'is_verified',
        'is_active',
    ];

    protected function casts(): array
    {
        return [
            'latitude' => 'float',
            'longitude' => 'float',
            'price_per_hour' => 'decimal:2',
            'is_verified' => 'boolean',
            'is_active' => 'boolean',
        ];
    }

    public function scopeActive(Builder $query): Builder
    {
        return $query->where('is_active', true);
    }

    public function scopeVerified(Builder $query): Builder
    {
        return $query->where('is_verified', true);
    }

    public function scopeNearby(Builder $query, float $latitude, float $longitude, float $radiusKm = 10): Builder
    {
        $haversine = '(6371 * acos(cos(radians(?)) * cos(radians(latitude)) * cos(radians(longitude) - radians(?)) + sin(radians(?)) * sin(radians(latitude))))';

        return $query
            ->selectRaw("*, {$haversine} AS distance_km", [$latitude, $longitude, $latitude])
            ->whereRaw("{$haversine} <= ?", [$latitude, $longitude, $latitude, $radiusKm])
            ->orderByRaw($haversine, [$latitude, $longitude, $latitude]);
    }

    public function owner()
    {
        return $this->belongsTo(User::class, 'owner_id');
    }

    public function images()
    {
        return $this->hasMany(ParkingImage::class);
    }

    public function bookings()
    {
        return $this->hasMany(Booking::class);
    }

    public function reviews()
    {
        return $this->hasMany(Review::class);
    }

    public function favorites()
    {
        return $this->hasMany(FavoriteParking::class);
    }

    public function availability()
    {
        return $this->hasMany(ParkingAvailability::class);
    }

    public function reports()
    {
        return $this->hasMany(Report::class);
    }
}
