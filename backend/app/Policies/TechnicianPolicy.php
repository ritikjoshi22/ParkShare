<?php

namespace App\Policies;

use App\Models\Technician;
use App\Models\User;

class TechnicianPolicy
{
    public function viewAny(?User $user): bool
    {
        return true;
    }

    public function view(?User $user, Technician $technician): bool
    {
        return true;
    }

    public function create(User $user): bool
    {
        return $user->isTechnician() || $user->isAdmin();
    }

    public function update(User $user, Technician $technician): bool
    {
        return $user->isAdmin() || $technician->user_id === $user->id;
    }

    public function delete(User $user, Technician $technician): bool
    {
        return $user->isAdmin() || $technician->user_id === $user->id;
    }
}
