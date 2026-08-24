<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureApprovedOwner
{
    public function handle(Request $request, Closure $next): Response
    {
        $user = $request->user();

        if (! $user || ! $user->hasOwnerCapability()) {
            return response()->json([
                'success' => false,
                'message' => 'Approved owner access required. Complete owner verification first.',
            ], 403);
        }

        return $next($request);
    }
}
