<?php

namespace App\Http\Controllers\Api;

use App\Http\Resources\ParkingTechnicianResource;
use App\Models\ParkingSpace;
use App\Models\ParkingTechnician;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class ParkingTechnicianController extends ApiController
{
    public function index(Request $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('view', $parkingSpace);

        $technicians = $parkingSpace->parkingTechnicians()
            ->where('is_active', true)
            ->orderByDesc('is_primary')
            ->orderBy('name')
            ->get();

        return $this->success(ParkingTechnicianResource::collection($technicians));
    }

    public function ownerIndex(Request $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('update', $parkingSpace);

        $technicians = $parkingSpace->parkingTechnicians()
            ->orderByDesc('is_primary')
            ->orderBy('name')
            ->get();

        return $this->success(ParkingTechnicianResource::collection($technicians));
    }

    public function store(Request $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('update', $parkingSpace);

        $validated = $request->validate([
            'name' => ['required', 'string', 'max:255'],
            'phone' => ['required', 'string', 'max:20'],
            'alternate_phone' => ['nullable', 'string', 'max:20'],
            'email' => ['nullable', 'email', 'max:255'],
            'specialization' => ['required', 'string', Rule::in($this->specializations())],
            'description' => ['nullable', 'string', 'max:1000'],
            'availability_status' => ['nullable', 'string', Rule::in(['available', 'busy', 'offline'])],
            'is_primary' => ['sometimes', 'boolean'],
        ]);

        if ($validated['is_primary'] ?? false) {
            $parkingSpace->parkingTechnicians()->update(['is_primary' => false]);
        }

        $technician = $parkingSpace->parkingTechnicians()->create([
            ...$validated,
            'availability_status' => $validated['availability_status'] ?? 'available',
            'is_primary' => $validated['is_primary'] ?? false,
            'is_active' => true,
        ]);

        return $this->success(new ParkingTechnicianResource($technician), 'Technician added.', 201);
    }

    public function update(Request $request, ParkingSpace $parkingSpace, ParkingTechnician $parkingTechnician): JsonResponse
    {
        $this->authorize('update', $parkingSpace);
        $this->ensureTechnicianBelongsToParking($parkingTechnician, $parkingSpace);

        $validated = $request->validate([
            'name' => ['sometimes', 'string', 'max:255'],
            'phone' => ['sometimes', 'string', 'max:20'],
            'alternate_phone' => ['nullable', 'string', 'max:20'],
            'email' => ['nullable', 'email', 'max:255'],
            'specialization' => ['sometimes', 'string', Rule::in($this->specializations())],
            'description' => ['nullable', 'string', 'max:1000'],
            'availability_status' => ['sometimes', 'string', Rule::in(['available', 'busy', 'offline'])],
            'is_primary' => ['sometimes', 'boolean'],
            'is_active' => ['sometimes', 'boolean'],
        ]);

        if ($validated['is_primary'] ?? false) {
            $parkingSpace->parkingTechnicians()
                ->where('id', '!=', $parkingTechnician->id)
                ->update(['is_primary' => false]);
        }

        $parkingTechnician->update($validated);

        return $this->success(new ParkingTechnicianResource($parkingTechnician->fresh()), 'Technician updated.');
    }

    public function destroy(Request $request, ParkingSpace $parkingSpace, ParkingTechnician $parkingTechnician): JsonResponse
    {
        $this->authorize('update', $parkingSpace);
        $this->ensureTechnicianBelongsToParking($parkingTechnician, $parkingSpace);

        $parkingTechnician->delete();

        return $this->success(null, 'Technician removed.');
    }

    protected function ensureTechnicianBelongsToParking(ParkingTechnician $technician, ParkingSpace $parkingSpace): void
    {
        if ($technician->parking_space_id !== $parkingSpace->id) {
            abort(404);
        }
    }

    /** @return list<string> */
    protected function specializations(): array
    {
        return [
            'general_mechanic',
            'auto_electrician',
            'tyre_repair',
            'battery_service',
            'ev_technician',
            'towing_service',
            'roadside_assistance',
            'other',
        ];
    }
}
