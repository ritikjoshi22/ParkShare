<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Technician\StoreTechnicianServiceRequest;
use App\Http\Resources\TechnicianServiceResource;
use App\Models\Technician;
use App\Models\TechnicianService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class TechnicianServiceController extends ApiController
{
    public function index(Request $request, Technician $technician): JsonResponse
    {
        $services = $technician->services()->paginate($request->integer('per_page', 15));

        return $this->success(TechnicianServiceResource::collection($services));
    }

    public function store(StoreTechnicianServiceRequest $request, Technician $technician): JsonResponse
    {
        $this->authorize('update', $technician);
        $this->authorize('create', TechnicianService::class);

        if ($request->user()->isTechnician() && $technician->user_id !== $request->user()->id) {
            abort(403);
        }

        $service = $technician->services()->create($request->validated());

        return $this->success(new TechnicianServiceResource($service), 'Service added.', 201);
    }

    public function destroy(TechnicianService $technicianService): JsonResponse
    {
        $this->authorize('delete', $technicianService);

        $technicianService->delete();

        return $this->success(null, 'Service removed.');
    }
}
