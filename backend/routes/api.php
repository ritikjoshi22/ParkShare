<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\BookingController;
use App\Http\Controllers\Api\FavoriteParkingController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\OwnerStatsController;
use App\Http\Controllers\Api\ParkingAvailabilityController;
use App\Http\Controllers\Api\ParkingImageController;
use App\Http\Controllers\Api\ParkingSpaceController;
use App\Http\Controllers\Api\ReportController;
use App\Http\Controllers\Api\ReviewController;
use App\Http\Controllers\Api\SOSRequestController;
use App\Http\Controllers\Api\TechnicianController;
use App\Http\Controllers\Api\TechnicianServiceController;
use App\Http\Controllers\Api\UserController;
use Illuminate\Support\Facades\Route;

Route::prefix('auth')->group(function () {
    Route::post('register', [AuthController::class, 'register']);
    Route::post('login', [AuthController::class, 'login']);

    Route::middleware(['auth:sanctum', 'active'])->group(function () {
        Route::post('logout', [AuthController::class, 'logout']);
        Route::get('profile', [AuthController::class, 'profile']);
        Route::put('profile', [AuthController::class, 'updateProfile']);
    });
});

Route::get('parking-spaces/nearby', [ParkingSpaceController::class, 'nearby']);

Route::middleware(['auth:sanctum', 'active'])->group(function () {
    Route::middleware('role:admin')->group(function () {
        Route::get('users', [UserController::class, 'index']);
        Route::get('users/{user}', [UserController::class, 'show']);
        Route::put('users/{user}', [UserController::class, 'update']);
        Route::delete('users/{user}', [UserController::class, 'destroy']);
    });

    Route::get('owner/stats', [OwnerStatsController::class, 'index']);

    Route::apiResource('parking-spaces', ParkingSpaceController::class);
    Route::post('parking-spaces/{parking_space}/images', [ParkingImageController::class, 'store']);
    Route::delete('parking-images/{parking_image}', [ParkingImageController::class, 'destroy']);

    Route::get('parking-spaces/{parking_space}/availability', [ParkingAvailabilityController::class, 'index']);
    Route::post('parking-spaces/{parking_space}/availability', [ParkingAvailabilityController::class, 'store']);
    Route::delete('parking-availability/{parking_availability}', [ParkingAvailabilityController::class, 'destroy']);

    Route::get('parking-spaces/{parking_space}/bookings', [BookingController::class, 'ownerList']);

    Route::middleware('throttle:bookings')->group(function () {
        Route::get('bookings', [BookingController::class, 'index']);
        Route::post('bookings', [BookingController::class, 'store']);
        Route::get('bookings/history', [BookingController::class, 'history']);
        Route::get('bookings/{booking}', [BookingController::class, 'show']);
        Route::post('bookings/{booking}/cancel', [BookingController::class, 'cancel']);
    });

    Route::apiResource('reviews', ReviewController::class)->only(['index', 'store', 'destroy']);

    Route::get('technicians/profile', [TechnicianController::class, 'profile']);
    Route::apiResource('technicians', TechnicianController::class)->only(['index', 'show', 'store', 'update']);
    Route::get('technicians/{technician}/services', [TechnicianServiceController::class, 'index']);
    Route::post('technicians/{technician}/services', [TechnicianServiceController::class, 'store']);
    Route::delete('technician-services/{technician_service}', [TechnicianServiceController::class, 'destroy']);

    Route::middleware('throttle:sos')->group(function () {
        Route::get('sos-requests', [SOSRequestController::class, 'index']);
        Route::post('sos-requests', [SOSRequestController::class, 'store']);
        Route::get('sos-requests/history', [SOSRequestController::class, 'history']);
        Route::get('sos-requests/{sos_request}', [SOSRequestController::class, 'show']);
        Route::post('sos-requests/{sos_request}/assign', [SOSRequestController::class, 'assign']);
        Route::patch('sos-requests/{sos_request}/status', [SOSRequestController::class, 'updateStatus']);
    });

    Route::get('notifications', [NotificationController::class, 'index']);
    Route::post('notifications/read-all', [NotificationController::class, 'markAllAsRead']);
    Route::patch('notifications/{notification}/read', [NotificationController::class, 'markAsRead']);

    Route::apiResource('favorites', FavoriteParkingController::class)
        ->only(['index', 'store', 'destroy'])
        ->parameters(['favorites' => 'favoriteParking']);

    //
    Route::get('reports', [ReportController::class, 'index']);
    Route::post('reports', [ReportController::class, 'store']);
    Route::get('reports/{report}', [ReportController::class, 'show']);
    Route::delete('reports/{report}', [ReportController::class, 'destroy'])->middleware('role:admin');
    Route::post('reports/{report}/resolve', [ReportController::class, 'resolve'])->middleware('role:admin');
});
