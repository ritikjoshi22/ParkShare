<?php

namespace App\Policies;

use App\Models\SOSRequest;
use App\Models\User;

class SOSRequestPolicy
{
    public function viewAny(User $user): bool
    {
        return true;
    }

    public function view(User $user, SOSRequest $sos): bool
    {
        if ($user->isAdmin()) {
            return true;
        }

        if ($sos->user_id === $user->id) {
            return true;
        }

        return $user->isTechnician()
            && $sos->technician?->user_id === $user->id;
    }

    public function create(User $user): bool
    {
        return $user->isDriver() || $user->isAdmin();
    }

    public function assign(User $user): bool
    {
        return $user->isAdmin() || $user->isTechnician();
    }

    public function update(User $user, SOSRequest $sos): bool
    {
        if ($user->isAdmin()) {
            return true;
        }

        if ($sos->user_id === $user->id) {
            return true;
        }

        return $user->isTechnician()
            && $sos->technician?->user_id === $user->id;
    }
}
