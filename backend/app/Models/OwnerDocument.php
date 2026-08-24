<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class OwnerDocument extends Model
{
    protected $fillable = [
        'owner_profile_id',
        'document_type',
        'file_path',
        'original_name',
        'mime_type',
        'file_size',
        'status',
        'rejection_reason',
    ];

    public function ownerProfile()
    {
        return $this->belongsTo(OwnerProfile::class);
    }
}
