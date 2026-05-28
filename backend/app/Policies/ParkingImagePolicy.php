<?php

namespace App\Policies;

use App\Models\ParkingImage;
use App\Models\User;

class ParkingImagePolicy
{
    public function create(User $user): bool
    {
        return $user->isOwner() || $user->isAdmin();
    }

    public function delete(User $user, ParkingImage $image): bool
    {
        return $user->isAdmin()
            || $image->parkingSpace?->owner_id === $user->id;
    }
}
