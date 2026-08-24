<?php

namespace App\Http\Controllers\Api;

use App\Http\Resources\OwnerProfileResource;
use App\Models\OwnerDocument;
use App\Services\OwnerVerificationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class OwnerVerificationController extends ApiController
{
    public function __construct(
        protected OwnerVerificationService $verificationService
    ) {}

    public function status(Request $request): JsonResponse
    {
        $user = $request->user()->load('ownerProfile.documents');

        return $this->success([
            'user' => [
                'id' => $user->id,
                'full_name' => $user->full_name,
            ],
            ...$this->verificationService->statusPayload($user),
            'profile' => $user->ownerProfile
                ? new OwnerProfileResource($user->ownerProfile)
                : null,
        ], 'Owner status retrieved successfully.');
    }

    public function show(Request $request): JsonResponse
    {
        $profile = $this->verificationService->getOrCreateProfile($request->user());
        $profile->load('documents');

        return $this->success(new OwnerProfileResource($profile));
    }

    public function saveStep(Request $request, int $step): JsonResponse
    {
        $validated = $request->validate([
            'pan_number' => ['sometimes', 'nullable', 'string', 'max:10'],
            'consent' => ['sometimes', 'boolean'],
            'latitude' => ['sometimes', 'nullable', 'numeric', 'between:-90,90'],
            'longitude' => ['sometimes', 'nullable', 'numeric', 'between:-180,180'],
            'address' => ['sometimes', 'nullable', 'string', 'max:500'],
            'feature_covered' => ['sometimes', 'boolean'],
            'feature_cctv' => ['sometimes', 'boolean'],
            'feature_security' => ['sometimes', 'boolean'],
            'feature_ev' => ['sometimes', 'boolean'],
        ]);

        $profile = $this->verificationService->saveStep($request->user(), $step, $validated);

        return $this->success(new OwnerProfileResource($profile), 'Verification step saved.');
    }

    public function uploadDocument(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'document_type' => ['required', 'string'],
            'file' => ['required', 'file', 'mimes:jpg,jpeg,png,pdf', 'max:10240'],
        ]);

        $document = $this->verificationService->uploadDocument(
            $request->user(),
            $validated['document_type'],
            $validated['file']
        );

        return $this->success(new \App\Http\Resources\OwnerDocumentResource($document), 'Document uploaded.', 201);
    }

    public function deleteDocument(Request $request, OwnerDocument $ownerDocument): JsonResponse
    {
        $this->verificationService->deleteDocument($request->user(), $ownerDocument);

        return $this->success(null, 'Document deleted.');
    }

    public function submit(Request $request): JsonResponse
    {
        $profile = $this->verificationService->submit($request->user());

        return $this->success(new OwnerProfileResource($profile->load('documents')), 'Verification submitted for review.');
    }

    public function dashboard(Request $request): JsonResponse
    {
        if (! $request->user()->hasOwnerCapability()) {
            return $this->error('Approved owner access required.', 403);
        }

        $user = $request->user();
        $parkingCount = $user->parkingSpaces()->count();

        return $this->success([
            'parking_spaces_count' => $parkingCount,
            'owner_status' => $user->ownerProfile?->status ?? 'approved',
        ], 'Owner dashboard data retrieved.');
    }
}
