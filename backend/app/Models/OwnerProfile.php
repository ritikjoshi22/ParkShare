<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class OwnerProfile extends Model
{
    public const STATUS_DRAFT = 'draft';

    public const STATUS_SUBMITTED = 'submitted';

    public const STATUS_UNDER_REVIEW = 'under_review';

    public const STATUS_APPROVED = 'approved';

    public const STATUS_REJECTED = 'rejected';

    protected $fillable = [
        'user_id',
        'status',
        'current_step',
        'step_data',
        'submitted_at',
        'verified_at',
        'rejected_at',
        'rejection_reason',
    ];

    protected function casts(): array
    {
        return [
            'step_data' => 'array',
            'submitted_at' => 'datetime',
            'verified_at' => 'datetime',
            'rejected_at' => 'datetime',
        ];
    }

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function documents()
    {
        return $this->hasMany(OwnerDocument::class);
    }

    public function isApproved(): bool
    {
        return $this->status === self::STATUS_APPROVED;
    }

    public function isPendingReview(): bool
    {
        return in_array($this->status, [self::STATUS_SUBMITTED, self::STATUS_UNDER_REVIEW], true);
    }
}
