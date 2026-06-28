<?php

namespace App\Http\Controllers\Api;

use App\Services\SystemSettingsService;
use Illuminate\Http\JsonResponse;

class SystemSettingsController extends ApiController
{
    public function bookingRules(SystemSettingsService $settings): JsonResponse
    {
        return $this->success([
            'early_check_in_buffer_minutes' => $settings->getInt('early_check_in_buffer_minutes'),
            'late_check_in_grace_minutes' => $settings->getInt('late_check_in_grace_minutes'),
            'exit_grace_minutes' => $settings->getInt('exit_grace_minutes'),
            'booking_buffer_minutes' => $settings->getInt('booking_buffer_minutes'),
            'fine_per_15_minutes' => $settings->get('fine_per_15_minutes'),
            'extension_reminder_minutes' => $settings->getInt('extension_reminder_minutes'),
        ]);
    }
}
