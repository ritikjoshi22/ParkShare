<?php

namespace App\Policies;

use App\Models\TechnicianService;
use App\Models\User;

class TechnicianServicePolicy
{
    public function viewAny(?User $user): bool
    {
        return true;
    }

    public function create(User $user): bool
    {
        return $user->isTechnician() || $user->isAdmin();
    }

    public function update(User $user, TechnicianService $service): bool
    {
        return $user->isAdmin() || $service->technician?->user_id === $user->id;
    }

    public function delete(User $user, TechnicianService $service): bool
    {
        return $user->isAdmin() || $service->technician?->user_id === $user->id;
    }
}
