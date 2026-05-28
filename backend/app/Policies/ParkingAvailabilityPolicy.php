<?php

namespace App\Policies;

use App\Models\ParkingAvailability;
use App\Models\User;

class ParkingAvailabilityPolicy
{
    public function viewAny(?User $user): bool
    {
        return true;
    }

    public function create(User $user): bool
    {
        return $user->isOwner() || $user->isAdmin();
    }

    public function update(User $user, ParkingAvailability $availability): bool
    {
        return $user->isAdmin()
            || $availability->parkingSpace?->owner_id === $user->id;
    }

    public function delete(User $user, ParkingAvailability $availability): bool
    {
        return $this->update($user, $availability);
    }
}
