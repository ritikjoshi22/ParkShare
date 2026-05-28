<?php

namespace App\Policies;

use App\Models\ParkingSpace;
use App\Models\User;

class ParkingSpacePolicy
{
    public function viewAny(?User $user): bool
    {
        return true;
    }

    public function view(?User $user, ParkingSpace $parkingSpace): bool
    {
        return true;
    }

    public function create(User $user): bool
    {
        return $user->isOwner() || $user->isAdmin();
    }

    public function update(User $user, ParkingSpace $parkingSpace): bool
    {
        return $user->isAdmin() || ($user->isOwner() && $parkingSpace->owner_id === $user->id);
    }

    public function delete(User $user, ParkingSpace $parkingSpace): bool
    {
        return $user->isAdmin() || ($user->isOwner() && $parkingSpace->owner_id === $user->id);
    }

    public function manageImages(User $user, ParkingSpace $parkingSpace): bool
    {
        return $this->update($user, $parkingSpace);
    }

    public function viewOwnerBookings(User $user, ParkingSpace $parkingSpace): bool
    {
        return $user->isAdmin() || ($user->isOwner() && $parkingSpace->owner_id === $user->id);
    }
}
