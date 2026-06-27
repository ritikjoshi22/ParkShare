<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;

class Booking extends Model
{
    protected $fillable = [
        'user_id',
        'parking_space_id',
        'parking_slot_id',
        'booking_date',
        'start_time',
        'end_time',
        'total_hours',
        'total_amount',
        'booking_status',
        'qr_code',
        'checked_in_at',
        'checked_out_at',
        'overtime_fee',
        'payment_status',
        'amount_due',
        'original_end_time',
        'extended_minutes',
    ];

    protected function casts(): array
    {
        return [
            'booking_date' => 'date',
            'start_time' => 'datetime',
            'end_time' => 'datetime',
            'total_hours' => 'decimal:2',
            'total_amount' => 'decimal:2',
            'overtime_fee' => 'decimal:2',
            'amount_due' => 'decimal:2',
            'original_end_time' => 'datetime',
            'checked_in_at' => 'datetime',
            'checked_out_at' => 'datetime',
        ];
    }

    public function scopeActive(Builder $query): Builder
    {
        return $query->whereNotIn('booking_status', ['cancelled', 'completed']);
    }

    public function scopeForParking(Builder $query, int $parkingSpaceId): Builder
    {
        return $query->where('parking_space_id', $parkingSpaceId);
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function parkingSpace()
    {
        return $this->belongsTo(ParkingSpace::class);
    }

    public function parkingSlot()
    {
        return $this->belongsTo(ParkingSlot::class);
    }

    public function reports()
    {
        return $this->hasMany(Report::class);
    }

    public function payments()
    {
        return $this->hasMany(BookingPayment::class);
    }
}
