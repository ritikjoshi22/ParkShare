<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Auth\LoginRequest;
use App\Http\Requests\Auth\RegisterRequest;
use App\Http\Requests\Auth\UpdateProfileRequest;
use App\Http\Resources\UserResource;
use App\Models\Technician;
use App\Models\User;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\ValidationException;

class AuthController extends ApiController
{
    public function register(RegisterRequest $request): JsonResponse
    {
        $data = $request->validated();
        $data['password'] = Hash::make($data['password']);

        $user = User::create($data);

        if ($user->isTechnician()) {
            Technician::create([
                'user_id' => $user->id,
                'specialization' => 'General',
                'experience_years' => 0,
                'service_radius_km' => 10,
                'availability_status' => 'offline',
            ]);
        }

        $user->load(['technician', 'ownerProfile']);

        $token = $user->createToken('api-token')->plainTextToken;

        return $this->success([
            'user' => new UserResource($user),
            'token' => $token,
            'token_type' => 'Bearer',
        ], 'Registration successful.', 201);
    }

    public function login(LoginRequest $request): JsonResponse
    {
        $user = User::where('email', $request->email)->first();

        if (! $user || ! Hash::check($request->password, $user->password)) {
            throw ValidationException::withMessages([
                'email' => ['Invalid credentials.'],
            ]);
        }

        if (! $user->is_active) {
            return $this->error('Account is deactivated.', 403);
        }

        $user->load(['technician', 'ownerProfile']);
        $token = $user->createToken('api-token')->plainTextToken;

        return $this->success([
            'user' => new UserResource($user),
            'token' => $token,
            'token_type' => 'Bearer',
        ], 'Login successful.');
    }

    public function logout(Request $request): JsonResponse
    {
        $request->user()->currentAccessToken()?->delete();

        return $this->success(null, 'Logged out successfully.');
    }

    public function profile(Request $request): JsonResponse
    {
        $user = $request->user()->load(['technician.services', 'ownerProfile']);

        return $this->success(new UserResource($user));
    }

    public function updateProfile(UpdateProfileRequest $request): JsonResponse
    {
        $user = $request->user();
        $data = $request->validated();

        if (isset($data['password'])) {
            $data['password'] = Hash::make($data['password']);
        }

        $user->update($data);

        return $this->success(new UserResource($user->fresh(['technician', 'ownerProfile'])), 'Profile updated.');
    }
}
