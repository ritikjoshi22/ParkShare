<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Technician\StoreTechnicianRequest;
use App\Http\Resources\TechnicianResource;
use App\Models\Technician;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class TechnicianController extends ApiController
{
    public function index(Request $request): JsonResponse
    {
        $technicians = Technician::with(['user', 'services'])
            ->withAvg('reviews', 'rating')
            ->when($request->status, fn ($q, $s) => $q->where('availability_status', $s))
            ->paginate($request->integer('per_page', 15));

        return $this->success(TechnicianResource::collection($technicians));
    }

    public function show(Technician $technician): JsonResponse
    {
        $this->authorize('view', $technician);

        $technician->load(['user', 'services', 'reviews.user'])->loadAvg('reviews', 'rating');

        return $this->success(new TechnicianResource($technician));
    }

    public function store(StoreTechnicianRequest $request): JsonResponse
    {
        $this->authorize('create', Technician::class);

        $user = $request->user();

        if ($user->technician) {
            return $this->error('Technician profile already exists.', 422);
        }

        $technician = Technician::create([
            ...$request->validated(),
            'user_id' => $user->id,
        ]);

        return $this->success(
            new TechnicianResource($technician->load('user')),
            'Technician profile created.',
            201
        );
    }

    public function update(StoreTechnicianRequest $request, Technician $technician): JsonResponse
    {
        $this->authorize('update', $technician);

        $technician->update($request->validated());

        return $this->success(new TechnicianResource($technician->fresh()->load('user', 'services')));
    }

    public function profile(Request $request): JsonResponse
    {
        $technician = $request->user()->technician;

        if (! $technician) {
            return $this->error('Technician profile not found.', 404);
        }

        return $this->success(new TechnicianResource(
            $technician->load(['user', 'services'])->loadAvg('reviews', 'rating')
        ));
    }
}
