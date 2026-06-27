<?php

namespace Database\Seeders;

use App\Models\PricingRule;
use App\Services\SystemSettingsService;
use Illuminate\Database\Seeder;

class BookingEngineSeeder extends Seeder
{
    public function run(): void
    {
        $settings = app(SystemSettingsService::class);

        foreach (SystemSettingsService::DEFAULTS as $key => $value) {
            $settings->set($key, $value, str_replace('_', ' ', ucfirst($key)));
        }

        if (PricingRule::query()->whereNull('parking_space_id')->exists()) {
            return;
        }

        $tiers = [
            ['hour_from' => 1, 'hour_to' => 1, 'rate' => 100],
            ['hour_from' => 2, 'hour_to' => 2, 'rate' => 90],
            ['hour_from' => 3, 'hour_to' => 3, 'rate' => 80],
            ['hour_from' => 4, 'hour_to' => null, 'rate' => 75],
        ];

        foreach ($tiers as $i => $tier) {
            PricingRule::create([
                'parking_space_id' => null,
                'hour_from' => $tier['hour_from'],
                'hour_to' => $tier['hour_to'],
                'rate' => $tier['rate'],
                'rule_type' => 'hourly',
                'daily_cap' => 600,
                'is_active' => true,
                'sort_order' => $i,
            ]);
        }
    }
}
