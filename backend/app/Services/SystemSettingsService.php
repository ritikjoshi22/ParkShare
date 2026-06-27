<?php

namespace App\Services;

use App\Models\SystemSetting;
use Illuminate\Support\Facades\Cache;

class SystemSettingsService
{
    public const CACHE_KEY = 'parkshare.system_settings';

    public const DEFAULTS = [
        'early_check_in_buffer_minutes' => 15,
        'late_check_in_grace_minutes' => 30,
        'exit_grace_minutes' => 10,
        'booking_buffer_minutes' => 15,
        'fine_per_15_minutes' => 20,
        'extension_reminder_minutes' => 20,
        'stripe_currency' => 'usd',
    ];

    public function all(): array
    {
        return Cache::rememberForever(self::CACHE_KEY, function () {
            $stored = SystemSetting::query()->pluck('value', 'key')->map(function ($value) {
                return is_array($value) ? ($value['value'] ?? $value) : $value;
            })->all();

            return array_merge(self::DEFAULTS, $stored);
        });
    }

    public function get(string $key, mixed $default = null): mixed
    {
        $all = $this->all();

        return $all[$key] ?? $default ?? (self::DEFAULTS[$key] ?? null);
    }

    public function getInt(string $key): int
    {
        return (int) $this->get($key);
    }

    public function set(string $key, mixed $value, ?string $label = null, string $group = 'booking'): void
    {
        SystemSetting::updateOrCreate(
            ['key' => $key],
            [
                'value' => ['value' => $value],
                'label' => $label ?? $key,
                'group' => $group,
            ]
        );

        Cache::forget(self::CACHE_KEY);
    }

    public function flush(): void
    {
        Cache::forget(self::CACHE_KEY);
    }
}
