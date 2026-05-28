<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\SOS\StoreSOSRequest;
use App\Http\Requests\SOS\UpdateSOSStatusRequest;
use App\Http\Resources\SOSRequestResource;
use App\Models\SOSRequest;
use App\Services\NotificationService;
use App\Services\SOSService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class SOSRequestController extends ApiController
{
    public function __construct(
        protected SOSService $sosService,
        protected NotificationService $notificationService
    ) {}

    public function index(Request $request): JsonResponse
    {
        $this->authorize('viewAny', SOSRequest::class);

        $query = SOSRequest::with(['user', 'technician.user'])->latest();

        if ($request->user()->isDriver()) {
            $query->where('user_id', $request->user()->id);
        } elseif ($request->user()->isTechnician()) {
            $query->whereHas('technician', fn ($q) => $q->where('user_id', $request->user()->id));
        }

        if ($request->status) {
            $query->where('status', $request->status);
        }

        return $this->success(SOSRequestResource::collection(
            $query->paginate($request->integer('per_page', 15))
        ));
    }

    public function store(StoreSOSRequest $request): JsonResponse
    {
        $this->authorize('create', SOSRequest::class);

        $sos = $this->sosService->create($request->user(), $request->validated());

        if ($sos->technician?->user) {
            $this->notificationService->notify(
                $sos->technician->user,
                'SOS assigned',
                'A new emergency SOS request has been assigned to you.',
                'sos'
            );
        }

        return $this->success(new SOSRequestResource($sos), 'SOS request created.', 201);
    }

    public function show(SOSRequest $sosRequest): JsonResponse
    {
        $this->authorize('view', $sosRequest);

        return $this->success(new SOSRequestResource(
            $sosRequest->load(['user', 'technician.user'])
        ));
    }

    public function assign(Request $request, SOSRequest $sosRequest): JsonResponse
    {
        $this->authorize('assign', SOSRequest::class);
        $this->authorize('update', $sosRequest);

        $request->validate([
            'technician_id' => ['nullable', 'integer', 'exists:technicians,id'],
        ]);

        $sos = $this->sosService->assignTechnician($sosRequest, $request->integer('technician_id') ?: null);

        return $this->success(new SOSRequestResource($sos), 'Technician assigned.');
    }

    public function updateStatus(UpdateSOSStatusRequest $request, SOSRequest $sosRequest): JsonResponse
    {
        $this->authorize('update', $sosRequest);

        $sos = $this->sosService->updateStatus($sosRequest, $request->validated('status'));

        return $this->success(new SOSRequestResource($sos), 'SOS status updated.');
    }

    public function history(Request $request): JsonResponse
    {
        $requests = SOSRequest::with(['technician.user'])
            ->where('user_id', $request->user()->id)
            ->whereIn('status', ['resolved', 'cancelled'])
            ->latest()
            ->paginate($request->integer('per_page', 15));

        return $this->success(SOSRequestResource::collection($requests));
    }
}
