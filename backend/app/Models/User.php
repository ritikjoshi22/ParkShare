<?php

namespace App\Models;

use App\Enums\UserRole;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable
{
    use HasApiTokens, HasFactory, Notifiable;

    protected $fillable = [
        'full_name',
        'email',
        'phone',
        'password',
        'role',
        'profile_image',
        'address',
        'latitude',
        'longitude',
        'is_active',
    ];

    protected $hidden = [
        'password',
        'remember_token',
    ];

    protected function casts(): array
    {
        return [
            'email_verified_at' => 'datetime',
            'password' => 'hashed',
            'is_active' => 'boolean',
            'latitude' => 'float',
            'longitude' => 'float',
        ];
    }

    public function isAdmin(): bool
    {
        return $this->role === UserRole::Admin->value;
    }

    public function isDriver(): bool
    {
        return $this->role === UserRole::Driver->value;
    }

    public function isOwner(): bool
    {
        return $this->role === UserRole::Owner->value;
    }

    public function isTechnician(): bool
    {
        return $this->role === UserRole::Technician->value;
    }

    public function parkingSpaces()
    {
        return $this->hasMany(ParkingSpace::class, 'owner_id');
    }

    public function bookings()
    {
        return $this->hasMany(Booking::class);
    }

    public function reviews()
    {
        return $this->hasMany(Review::class);
    }

    public function technician()
    {
        return $this->hasOne(Technician::class);
    }

    public function sosRequests()
    {
        return $this->hasMany(SOSRequest::class);
    }

    public function notifications()
    {
        return $this->hasMany(Notification::class);
    }

    public function favoriteParking()
    {
        return $this->hasMany(FavoriteParking::class);
    }

    public function reports()
    {
        return $this->hasMany(Report::class, 'reported_by');
    }
}
