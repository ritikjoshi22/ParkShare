<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Booking\StoreBookingRequest;
use App\Http\Resources\BookingResource;
use App\Models\Booking;
use App\Models\ParkingSpace;
use App\Services\BookingService;
use App\Services\NotificationService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class BookingController extends ApiController
{
    public function __construct(
        protected BookingService $bookingService,
        protected NotificationService $notificationService
    ) {}

    public function index(Request $request): JsonResponse
    {
        $this->authorize('viewAny', Booking::class);

        $query = Booking::with(['parkingSpace.images', 'user']);

        if ($request->user()->isDriver()) {
            $query->where('user_id', $request->user()->id);
        } elseif ($request->user()->isOwner()) {
            $query->whereHas('parkingSpace', fn ($q) => $q->where('owner_id', $request->user()->id));
        }

        if ($request->status) {
            $query->where('booking_status', $request->status);
        }

        $bookings = $query->latest()->paginate($request->integer('per_page', 15));

        return $this->success(BookingResource::collection($bookings));
    }

    public function store(StoreBookingRequest $request): JsonResponse
    {
        $this->authorize('create', Booking::class);

        $booking = $this->bookingService->create($request->user(), $request->validated());

        $owner = $booking->parkingSpace?->owner;
        if ($owner) {
            $this->notificationService->notify(
                $owner,
                'New booking',
                "New booking at {$booking->parkingSpace->parking_name}.",
                'booking'
            );
        }

        return $this->success(new BookingResource($booking), 'Booking created.', 201);
    }

    public function show(Booking $booking): JsonResponse
    {
        $this->authorize('view', $booking);

        return $this->success(new BookingResource($booking->load(['parkingSpace.images', 'user'])));
    }

    public function cancel(Booking $booking): JsonResponse
    {
        $this->authorize('cancel', $booking);

        $booking = $this->bookingService->cancel($booking, request()->user());

        return $this->success(new BookingResource($booking), 'Booking cancelled.');
    }

    public function history(Request $request): JsonResponse
    {
        $bookings = Booking::with(['parkingSpace.images'])
            ->where('user_id', $request->user()->id)
            ->latest()
            ->paginate($request->integer('per_page', 15));

        return $this->success(BookingResource::collection($bookings));
    }

    public function ownerList(Request $request, ParkingSpace $parkingSpace): JsonResponse
    {
        $this->authorize('viewOwnerBookings', $parkingSpace);

        $bookings = $parkingSpace->bookings()
            ->with('user')
            ->latest()
            ->paginate($request->integer('per_page', 15));

        return $this->success(BookingResource::collection($bookings));
    }
}
