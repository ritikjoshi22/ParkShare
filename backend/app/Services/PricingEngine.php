<?php

namespace App\Services;

use App\Models\ParkingSpace;
use App\Models\PricingRule;
use Carbon\Carbon;

class PricingEngine
{
    public function __construct(
        protected SystemSettingsService $settings
    ) {}

    /**
     * @return array{total: float, hours: float, breakdown: array<int, array{hour: int, rate: float, amount: float}>, daily_cap_applied: bool}
     */
    public function calculate(ParkingSpace $parking, Carbon $start, Carbon $end): array
    {
        $minutes = max(1, $start->diffInMinutes($end));
        $hours = round($minutes / 60, 2);
        $fullHours = (int) ceil($minutes / 60);

        $rules = PricingRule::query()
            ->where('is_active', true)
            ->where(function ($q) use ($parking) {
                $q->whereNull('parking_space_id')
                    ->orWhere('parking_space_id', $parking->id);
            })
            ->orderBy('sort_order')
            ->orderBy('hour_from')
            ->get();

        if ($rules->isEmpty()) {
            $total = round($hours * (float) $parking->price_per_hour, 2);

            return [
                'total' => $total,
                'hours' => $hours,
                'breakdown' => [[
                    'hour' => 1,
                    'rate' => (float) $parking->price_per_hour,
                    'amount' => $total,
                ]],
                'daily_cap_applied' => false,
            ];
        }

        $breakdown = [];
        $total = 0.0;
        $dailyCap = null;

        for ($h = 1; $h <= $fullHours; $h++) {
            $rule = $this->resolveRuleForHour($rules, $h);
            $rate = (float) $rule->rate;
            $dailyCap = $rule->daily_cap ? (float) $rule->daily_cap : $dailyCap;
            $breakdown[] = ['hour' => $h, 'rate' => $rate, 'amount' => $rate];
            $total += $rate;
        }

        $dailyCapApplied = false;
        if ($dailyCap !== null && $total > $dailyCap) {
            $total = $dailyCap;
            $dailyCapApplied = true;
        }

        return [
            'total' => round($total, 2),
            'hours' => $hours,
            'breakdown' => $breakdown,
            'daily_cap_applied' => $dailyCapApplied,
        ];
    }

    protected function resolveRuleForHour($rules, int $hour): PricingRule
    {
        $match = $rules->first(function (PricingRule $rule) use ($hour) {
            if ($hour < $rule->hour_from) {
                return false;
            }

            return $rule->hour_to === null || $hour <= $rule->hour_to;
        });

        return $match ?? $rules->last();
    }

    public function calculateOvertime(ParkingSpace $parking, Carbon $endTime, Carbon $checkoutTime): float
    {
        $exitGrace = $this->settings->getInt('exit_grace_minutes');
        $graceEnd = $endTime->copy()->addMinutes($exitGrace);

        if ($checkoutTime->lte($graceEnd)) {
            return 0.0;
        }

        $overtimeMinutes = $graceEnd->diffInMinutes($checkoutTime);
        $blocks = (int) ceil($overtimeMinutes / 15);
        $finePerBlock = (float) $this->settings->get('fine_per_15_minutes');

        return round($blocks * $finePerBlock, 2);
    }
}
