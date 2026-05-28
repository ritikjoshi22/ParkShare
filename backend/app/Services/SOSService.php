<?php

namespace App\Services;

use App\Models\SOSRequest;
use App\Models\Technician;
use App\Models\User;
use Carbon\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

class SOSService
{
    public function create(User $user, array $data): SOSRequest
    {
        $active = SOSRequest::where('user_id', $user->id)->active()->exists();

        if ($active) {
            throw ValidationException::withMessages([
                'sos' => ['You already have an active SOS request.'],
            ]);
        }

        return DB::transaction(function () use ($user, $data) {
            $sos = SOSRequest::create([
                'user_id' => $user->id,
                'latitude' => $data['latitude'],
                'longitude' => $data['longitude'],
                'emergency_message' => $data['emergency_message'] ?? null,
                'status' => 'active',
            ]);

            $technician = $this->findNearestAvailableTechnician(
                (float) $data['latitude'],
                (float) $data['longitude']
            );

            if ($technician) {
                $sos->update(['technician_id' => $technician->id]);
                $technician->update(['availability_status' => 'busy']);
            }

            return $sos->load(['technician.user', 'user']);
        });
    }

    public function assignTechnician(SOSRequest $sos, ?int $technicianId = null): SOSRequest
    {
        $technician = $technicianId
            ? Technician::available()->findOrFail($technicianId)
            : $this->findNearestAvailableTechnician((float) $sos->latitude, (float) $sos->longitude);

        if (! $technician) {
            throw ValidationException::withMessages([
                'technician' => ['No available technician found within service radius.'],
            ]);
        }

        if ($sos->technician_id && $sos->technician) {
            $sos->technician->update(['availability_status' => 'available']);
        }

        $sos->update(['technician_id' => $technician->id]);
        $technician->update(['availability_status' => 'busy']);

        return $sos->fresh(['technician.user', 'user']);
    }

    public function updateStatus(SOSRequest $sos, string $status): SOSRequest
    {
        $data = ['status' => $status];

        if ($status === 'resolved') {
            $data['resolved_at'] = Carbon::now();
            if ($sos->technician) {
                $sos->technician->update(['availability_status' => 'available']);
            }
        }

        $sos->update($data);

        return $sos->fresh(['technician.user', 'user']);
    }

    /**
     * Placeholder: Haversine distance to find nearest available technician.
     */
    protected function findNearestAvailableTechnician(float $latitude, float $longitude): ?Technician
    {
        $haversine = '(6371 * acos(cos(radians(?)) * cos(radians(users.latitude)) * cos(radians(users.longitude) - radians(?)) + sin(radians(?)) * sin(radians(users.latitude))))';

        return Technician::query()
            ->available()
            ->join('users', 'users.id', '=', 'technicians.user_id')
            ->whereNotNull('users.latitude')
            ->whereNotNull('users.longitude')
            ->select('technicians.*')
            ->selectRaw("technicians.*, {$haversine} AS distance_km", [$latitude, $longitude, $latitude])
            ->whereRaw("{$haversine} <= technicians.service_radius_km", [$latitude, $longitude, $latitude])
            ->orderBy('distance_km')
            ->first();
    }
}
