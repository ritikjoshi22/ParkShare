<?php

namespace App\Policies;

use App\Models\Report;
use App\Models\User;

class ReportPolicy
{
    public function viewAny(User $user): bool
    {
        return $user->isAdmin() || $user->isDriver();
    }

    public function view(User $user, Report $report): bool
    {
        return $user->isAdmin() || $report->reported_by === $user->id;
    }

    public function create(User $user): bool
    {
        return $user->isDriver() || $user->isAdmin();
    }

    public function resolve(User $user, Report $report): bool
    {
        return $user->isAdmin();
    }

    public function delete(User $user, Report $report): bool
    {
        return $user->isAdmin();
    }
}
