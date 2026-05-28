<?php

namespace App\Policies;

use App\Models\Booking;
use App\Models\User;

class BookingPolicy
{
    public function viewAny(User $user): bool
    {
        return true;
    }

    public function view(User $user, Booking $booking): bool
    {
        if ($user->isAdmin()) {
            return true;
        }

        if ($booking->user_id === $user->id) {
            return true;
        }

        return $user->isOwner()
            && $booking->parkingSpace?->owner_id === $user->id;
    }

    public function create(User $user): bool
    {
        return $user->isDriver() || $user->isAdmin();
    }

    public function update(User $user, Booking $booking): bool
    {
        return $user->isAdmin();
    }

    public function cancel(User $user, Booking $booking): bool
    {
        return $user->isAdmin() || $booking->user_id === $user->id;
    }

    public function delete(User $user, Booking $booking): bool
    {
        return $user->isAdmin();
    }
}
