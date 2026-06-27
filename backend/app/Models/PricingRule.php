<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class PricingRule extends Model
{
    protected $fillable = [
        'parking_space_id',
        'hour_from',
        'hour_to',
        'rate',
        'rule_type',
        'daily_cap',
        'is_active',
        'sort_order',
    ];

    protected function casts(): array
    {
        return [
            'rate' => 'decimal:2',
            'daily_cap' => 'decimal:2',
            'is_active' => 'boolean',
        ];
    }

    public function parkingSpace()
    {
        return $this->belongsTo(ParkingSpace::class);
    }
}
