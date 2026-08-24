<?php

namespace App\Services;

use App\Models\OwnerDocument;
use App\Models\OwnerProfile;
use App\Models\User;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Illuminate\Validation\ValidationException;

class OwnerVerificationService
{
    public const DOCUMENT_TYPES = [
        'pan_front', 'pan_back', 'license', 'selfie',
        'lalpurja', 'tax_receipt',
        'entry_gate', 'front_view', 'back_view', 'cctv',
    ];

    public const STEP_FIELDS = [
        1 => ['pan_number', 'consent'],
        2 => ['latitude', 'longitude', 'address'],
        3 => [],
        4 => ['feature_covered', 'feature_cctv', 'feature_security', 'feature_ev'],
    ];

    public function getOrCreateProfile(User $user): OwnerProfile
    {
        return OwnerProfile::firstOrCreate(
            ['user_id' => $user->id],
            ['status' => OwnerProfile::STATUS_DRAFT, 'current_step' => 1, 'step_data' => []]
        );
    }

    public function statusPayload(User $user): array
    {
        $profile = $user->ownerProfile;

        return [
            'capabilities' => [
                'driver' => $user->isDriver(),
                'owner' => $user->hasOwnerCapability(),
            ],
            'owner' => $profile ? [
                'status' => $profile->status,
                'current_step' => $profile->current_step,
                'submitted_at' => $profile->submitted_at?->toIso8601String(),
                'verified_at' => $profile->verified_at?->toIso8601String(),
                'rejected_at' => $profile->rejected_at?->toIso8601String(),
                'rejection_reason' => $profile->rejection_reason,
            ] : [
                'status' => null,
                'current_step' => 1,
            ],
        ];
    }

    public function saveStep(User $user, int $step, array $data): OwnerProfile
    {
        if ($step < 1 || $step > 4) {
            throw ValidationException::withMessages(['step' => ['Invalid verification step.']]);
        }

        $profile = $this->getOrCreateProfile($user);

        if ($profile->isPendingReview()) {
            throw ValidationException::withMessages([
                'verification' => ['Verification is pending review and cannot be edited.'],
            ]);
        }

        if ($profile->isApproved()) {
            throw ValidationException::withMessages([
                'verification' => ['You are already an approved owner.'],
            ]);
        }

        if ($profile->status === OwnerProfile::STATUS_REJECTED) {
            $profile->update(['status' => OwnerProfile::STATUS_DRAFT, 'rejection_reason' => null]);
        }

        $stepData = $profile->step_data ?? [];
        $stepData['step_'.$step] = array_merge($stepData['step_'.$step] ?? [], $data);
        $nextStep = max($profile->current_step, min(4, $step + 1));

        $profile->update([
            'step_data' => $stepData,
            'current_step' => $step,
            'status' => OwnerProfile::STATUS_DRAFT,
        ]);

        return $profile->fresh('documents');
    }

    public function uploadDocument(User $user, string $documentType, UploadedFile $file): OwnerDocument
    {
        if (! in_array($documentType, self::DOCUMENT_TYPES, true)) {
            throw ValidationException::withMessages(['document_type' => ['Invalid document type.']]);
        }

        $profile = $this->getOrCreateProfile($user);

        if ($profile->isPendingReview() || $profile->isApproved()) {
            throw ValidationException::withMessages(['document' => ['Cannot upload documents in current status.']]);
        }

        $path = $file->store("owner-documents/{$user->id}", 'local');

        OwnerDocument::where('owner_profile_id', $profile->id)
            ->where('document_type', $documentType)
            ->delete();

        return OwnerDocument::create([
            'owner_profile_id' => $profile->id,
            'document_type' => $documentType,
            'file_path' => $path,
            'original_name' => $file->getClientOriginalName(),
            'mime_type' => $file->getMimeType(),
            'file_size' => $file->getSize(),
            'status' => 'pending',
        ]);
    }

    public function deleteDocument(User $user, OwnerDocument $document): void
    {
        if ($document->ownerProfile?->user_id !== $user->id) {
            abort(403);
        }

        Storage::disk('local')->delete($document->file_path);
        $document->delete();
    }

    public function submit(User $user): OwnerProfile
    {
        $profile = $this->getOrCreateProfile($user);

        if ($profile->isApproved()) {
            throw ValidationException::withMessages(['verification' => ['Already approved.']]);
        }

        if ($profile->isPendingReview()) {
            throw ValidationException::withMessages(['verification' => ['Already submitted for review.']]);
        }

        $this->validateReadyForSubmit($profile);

        $profile->update([
            'status' => OwnerProfile::STATUS_UNDER_REVIEW,
            'submitted_at' => now(),
            'rejected_at' => null,
            'rejection_reason' => null,
        ]);

        return $profile->fresh('documents');
    }

    protected function validateReadyForSubmit(OwnerProfile $profile): void
    {
        $errors = [];
        $data = $profile->step_data ?? [];

        if (empty($data['step_1']['pan_number'] ?? null)) {
            $errors['pan_number'] = ['PAN number is required.'];
        }
        if (empty($data['step_1']['consent'] ?? false)) {
            $errors['consent'] = ['You must accept the verification consent.'];
        }

        $requiredDocs = ['pan_front', 'pan_back', 'selfie', 'lalpurja', 'tax_receipt', 'entry_gate'];
        $uploaded = $profile->documents()->pluck('document_type')->all();
        foreach ($requiredDocs as $doc) {
            if (! in_array($doc, $uploaded, true)) {
                $errors[$doc] = ["Missing required document: {$doc}."];
            }
        }

        if ($errors !== []) {
            throw ValidationException::withMessages($errors);
        }
    }

    public function approve(OwnerProfile $profile): OwnerProfile
    {
        return DB::transaction(function () use ($profile) {
            $profile->update([
                'status' => OwnerProfile::STATUS_APPROVED,
                'verified_at' => now(),
                'rejected_at' => null,
                'rejection_reason' => null,
            ]);

            return $profile->fresh();
        });
    }

    public function reject(OwnerProfile $profile, string $reason): OwnerProfile
    {
        $profile->update([
            'status' => OwnerProfile::STATUS_REJECTED,
            'rejected_at' => now(),
            'rejection_reason' => $reason,
        ]);

        return $profile->fresh();
    }
}
