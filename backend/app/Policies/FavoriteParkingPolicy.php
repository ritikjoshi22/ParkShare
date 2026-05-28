<?php

namespace App\Policies;

use App\Models\FavoriteParking;
use App\Models\User;

class FavoriteParkingPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->isDriver() || $user->isAdmin();
    }

    public function create(User $user): bool
    {
        return $user->isDriver() || $user->isAdmin();
    }

    public function delete(User $user, FavoriteParking $favorite): bool
    {
        return $user->isAdmin() || $favorite->user_id === $user->id;
    }
}
