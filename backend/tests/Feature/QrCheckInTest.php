<?php

namespace Tests\Feature;

use App\Models\Booking;
use App\Models\ParkingSpace;
use App\Models\User;
use App\Services\BookingQrService;
use App\Services\SystemSettingsService;
use Carbon\Carbon;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;

class QrCheckInTest extends TestCase
{
    use RefreshDatabase;

    protected User $owner;
    protected User $driver;
    protected ParkingSpace $parking;
    protected BookingQrService $qrService;

    protected function setUp(): void
    {
        parent::setUp();

        config(['app.timezone' => 'Asia/Kathmandu']);
        Carbon::setTestNow(Carbon::parse('2023-10-27 12:00:00', 'Asia/Kathmandu'));

        $this->owner = User::create([
            'full_name' => 'Owner',
            'email' => 'owner@example.com',
            'phone' => '9800000000',
            'password' => bcrypt('password'),
            'role' => 'owner',
            'is_active' => true,
        ]);
        $this->driver = User::create([
            'full_name' => 'Driver',
            'email' => 'driver@example.com',
            'phone' => '9800000001',
            'password' => bcrypt('password'),
            'role' => 'driver',
            'is_active' => true,
        ]);
        $this->parking = ParkingSpace::create([
            'owner_id' => $this->owner->id,
            'parking_name' => 'Test Parking',
            'address' => 'Test Address',
            'latitude' => 0,
            'longitude' => 0,
            'total_slots' => 10,
            'available_slots' => 10,
            'price_per_hour' => 100,
            'early_check_in_minutes' => 15,
            'opening_time' => '00:00',
            'closing_time' => '23:59',
            'is_active' => true,
        ]);

        $this->qrService = app(BookingQrService::class);

        // Mock settings
        app(SystemSettingsService::class)->set('early_check_in_buffer_minutes', 15);
        app(SystemSettingsService::class)->set('late_check_in_grace_minutes', 30);
    }

    public function test_check_in_validation_logic()
    {
        $bookingStart = Carbon::parse('2023-10-27 18:45:00', 'Asia/Kathmandu');
        $bookingEnd = $bookingStart->copy()->addHour();

        // Case 1: 6:29 PM - Reject
        $b1 = $this->createBooking($bookingStart, $bookingEnd);
        $qr1 = $this->qrService->buildPayload($b1);
        Carbon::setTestNow(Carbon::parse('2023-10-27 18:29:59', 'Asia/Kathmandu'));
        $this->actingAs($this->owner)
            ->postJson('/api/v1/bookings/scan', ['qr_payload' => $qr1])
            ->assertStatus(422)
            ->assertJsonPath('errors.booking.0', 'Check-in opens at 6:30 PM (15 min before booking).');

        // Case 2: 6:30 PM - Allow
        $b2 = $this->createBooking($bookingStart, $bookingEnd);
        $qr2 = $this->qrService->buildPayload($b2);
        Carbon::setTestNow(Carbon::parse('2023-10-27 18:30:00', 'Asia/Kathmandu'));
        $this->actingAs($this->owner)
            ->postJson('/api/v1/bookings/scan', ['qr_payload' => $qr2])
            ->assertStatus(200);

        // Case 3: 6:31 PM - Allow
        $b3 = $this->createBooking($bookingStart, $bookingEnd);
        $qr3 = $this->qrService->buildPayload($b3);
        Carbon::setTestNow(Carbon::parse('2023-10-27 18:31:00', 'Asia/Kathmandu'));
        $this->actingAs($this->owner)
            ->postJson('/api/v1/bookings/scan', ['qr_payload' => $qr3])
            ->assertStatus(200);

        // Case 4: 6:45 PM - Allow
        $b4 = $this->createBooking($bookingStart, $bookingEnd);
        $qr4 = $this->qrService->buildPayload($b4);
        Carbon::setTestNow(Carbon::parse('2023-10-27 18:45:00', 'Asia/Kathmandu'));
        $this->actingAs($this->owner)
            ->postJson('/api/v1/bookings/scan', ['qr_payload' => $qr4])
            ->assertStatus(200);

        // Case 5: 7:15 PM (Grace 30 mins) - Allow
        $b5 = $this->createBooking($bookingStart, $bookingEnd);
        $qr5 = $this->qrService->buildPayload($b5);
        Carbon::setTestNow(Carbon::parse('2023-10-27 19:15:00', 'Asia/Kathmandu'));
        $this->actingAs($this->owner)
            ->postJson('/api/v1/bookings/scan', ['qr_payload' => $qr5])
            ->assertStatus(200);

        // Case 6: 7:16 PM (Grace expired) - Reject
        $b6 = $this->createBooking($bookingStart, $bookingEnd);
        $qr6 = $this->qrService->buildPayload($b6);
        Carbon::setTestNow(Carbon::parse('2023-10-27 19:16:00', 'Asia/Kathmandu'));
        $this->actingAs($this->owner)
            ->postJson('/api/v1/bookings/scan', ['qr_payload' => $qr6])
            ->assertStatus(422)
            ->assertJsonPath('errors.booking.0', 'Check-in window closed. Late grace was 30 minutes after start.');
    }

    protected function createBooking(Carbon $start, Carbon $end): Booking
    {
        return Booking::create([
            'user_id' => $this->driver->id,
            'parking_space_id' => $this->parking->id,
            'booking_date' => $start->toDateString(),
            'start_time' => $start,
            'end_time' => $end,
            'booking_status' => 'confirmed',
            'payment_status' => 'paid',
            'amount_due' => 0,
            'total_amount' => 100,
            'total_hours' => 1,
        ]);
    }
}
