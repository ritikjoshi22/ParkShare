<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\BookingController;
use App\Http\Controllers\Api\FavoriteParkingController;
use App\Http\Controllers\Api\NotificationController;
use App\Http\Controllers\Api\OwnerStatsController;
use App\Http\Controllers\Api\OwnerVerificationController;
use App\Http\Controllers\Api\ParkingAvailabilityController;
use App\Http\Controllers\Api\ParkingImageController;
use App\Http\Controllers\Api\ParkingSlotController;
use App\Http\Controllers\Api\ParkingSpaceController;
use App\Http\Controllers\Api\ParkingTechnicianController;
use App\Http\Controllers\Api\ReportController;
use App\Http\Controllers\Api\ReviewController;
use App\Http\Controllers\Api\SOSRequestController;
use App\Http\Controllers\Api\SystemSettingsController;
use App\Http\Controllers\Api\TechnicianController;
use App\Http\Controllers\Api\TechnicianServiceController;
use App\Http\Controllers\Api\UserController;
use App\Http\Controllers\Api\WebhookController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return response()->json(['success' => true, 'message' => 'ParkShare API v1 is running']);
});

Route::post('webhooks/stripe', [WebhookController::class, 'handleStripe']);

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

    Route::prefix('owner')->group(function () {
        Route::get('status', [OwnerVerificationController::class, 'status']);
        Route::get('verification', [OwnerVerificationController::class, 'show']);
        Route::put('verification/step/{step}', [OwnerVerificationController::class, 'saveStep'])->whereNumber('step');
        Route::post('documents', [OwnerVerificationController::class, 'uploadDocument']);
        Route::delete('documents/{ownerDocument}', [OwnerVerificationController::class, 'deleteDocument']);
        Route::post('verification/submit', [OwnerVerificationController::class, 'submit']);

        Route::middleware('approved.owner')->group(function () {
            Route::get('dashboard', [OwnerVerificationController::class, 'dashboard']);
            Route::get('stats', [OwnerStatsController::class, 'index']);

            Route::get('parking-spaces/{parking_space}/technicians', [ParkingTechnicianController::class, 'ownerIndex']);
            Route::post('parking-spaces/{parking_space}/technicians', [ParkingTechnicianController::class, 'store']);
            Route::put('parking-spaces/{parking_space}/technicians/{parkingTechnician}', [ParkingTechnicianController::class, 'update']);
            Route::delete('parking-spaces/{parking_space}/technicians/{parkingTechnician}', [ParkingTechnicianController::class, 'destroy']);
        });
    });

    Route::get('parking-spaces/{parking_space}/technicians', [ParkingTechnicianController::class, 'index']);

    Route::apiResource('parking-spaces', ParkingSpaceController::class);
    Route::post('parking-spaces/{parking_space}/images', [ParkingImageController::class, 'store']);
    Route::post('parking-spaces/{parking_space}/images/batch', [ParkingImageController::class, 'storeBatch']);
    Route::patch('parking-images/{parking_image}/primary', [ParkingImageController::class, 'setPrimary']);
    Route::delete('parking-images/{parking_image}', [ParkingImageController::class, 'destroy']);

    Route::get('parking-spaces/{parking_space}/slots', [ParkingSlotController::class, 'index']);

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
        Route::post('bookings/scan', [BookingController::class, 'scan']);
        Route::post('bookings/quote', [BookingController::class, 'quote']);
        Route::get('bookings/active', [BookingController::class, 'active']);
        Route::get('bookings/{booking}/extension-options', [BookingController::class, 'extensionOptions']);
        Route::post('bookings/{booking}/extend', [BookingController::class, 'extend']);
        Route::post('bookings/{booking}/payment-intent', [BookingController::class, 'createPaymentIntent']);
        Route::post('bookings/{booking}/confirm-payment', [BookingController::class, 'confirmPayment']);
    });

    Route::get('settings/booking-rules', [SystemSettingsController::class, 'bookingRules']);

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

    Route::prefix('chat')->group(function () {
        Route::get('conversations', [\App\Http\Controllers\Api\ChatController::class, 'getConversations']);
        Route::get('messages/{booking?}', [\App\Http\Controllers\Api\ChatController::class, 'index']);
        Route::post('send', [\App\Http\Controllers\Api\ChatController::class, 'sendMessage']);
    });

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
