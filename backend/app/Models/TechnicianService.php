<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class TechnicianService extends Model
{
    protected $table = 'technicians_services';

    protected $fillable = [
        'technician_id',
        'service_name',
    ];

    public function technician()
    {
        return $this->belongsTo(Technician::class);
    }
}
