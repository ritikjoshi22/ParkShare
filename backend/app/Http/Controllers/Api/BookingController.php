<?php

namespace App\Http\Controllers\Api;

use App\Http\Requests\Booking\ScanBookingQrRequest;
use App\Http\Requests\Booking\StoreBookingRequest;
use App\Http\Resources\BookingResource;
use App\Http\Resources\BookingScanResource;
use App\Models\Booking;
use App\Models\ParkingSpace;
use App\Services\BookingExtensionService;
use App\Services\BookingService;
use App\Services\NotificationService;
use App\Services\PaymentService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class BookingController extends ApiController
{
    public function __construct(
        protected BookingService $bookingService,
        protected BookingExtensionService $extensionService,
        protected PaymentService $paymentService,
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
            ->with(['user', 'parkingSlot'])
            ->latest()
            ->paginate($request->integer('per_page', 15));

        return $this->success(BookingResource::collection($bookings));
    }

    public function scan(ScanBookingQrRequest $request): JsonResponse
    {
        $result = $this->bookingService->scanQr(
            $request->user(),
            $request->validated('qr_payload')
        );

        return $this->success(new BookingScanResource($result), $result['message']);
    }

    public function quote(Request $request): JsonResponse
    {
        $data = $request->validate([
            'parking_space_id' => ['required', 'integer', 'exists:parking_spaces,id'],
            'parking_slot_id' => ['nullable', 'integer', 'exists:parking_slots,id'],
            'start_time' => ['required', 'date', 'after:now'],
            'end_time' => ['required', 'date', 'after:start_time'],
        ]);

        $quote = $this->bookingService->quote($request->user(), $data);

        return $this->success($quote);
    }

    public function active(Request $request): JsonResponse
    {
        $booking = Booking::with(['parkingSpace.images', 'parkingSlot'])
            ->where('user_id', $request->user()->id)
            ->whereIn('booking_status', ['confirmed', 'checked_in'])
            ->orderBy('start_time')
            ->first();

        if (! $booking) {
            return $this->success(null, 'No active booking.');
        }

        return $this->success(new BookingResource($booking));
    }

    public function extensionOptions(Booking $booking): JsonResponse
    {
        $this->authorize('view', $booking);

        if ($booking->user_id !== request()->user()->id) {
            abort(403);
        }

        return $this->success($this->extensionService->getExtensionOptions($booking));
    }

    public function extend(Request $request, Booking $booking): JsonResponse
    {
        $this->authorize('view', $booking);

        if ($booking->user_id !== $request->user()->id) {
            abort(403);
        }

        $data = $request->validate([
            'minutes' => ['required', 'integer', Rule::in([15, 30, 45, 60, 90, 120])],
        ]);

        $booking = $this->extensionService->extend($booking, $data['minutes']);

        $this->notificationService->notify(
            $request->user(),
            'Booking extended',
            "Your booking was extended by {$data['minutes']} minutes.",
            'booking'
        );

        return $this->success(new BookingResource($booking), 'Booking extended successfully.');
    }

    public function createPaymentIntent(Request $request, Booking $booking): JsonResponse
    {
        $this->authorize('view', $booking);

        if ($booking->user_id !== $request->user()->id) {
            abort(403);
        }

        $data = $request->validate([
            'type' => ['required', Rule::in(['booking', 'extension', 'overtime', 'balance'])],
        ]);

        $amount = (float) $booking->amount_due;
        if ($amount <= 0) {
            return $this->error('No payment required.', 422);
        }

        $intent = $this->paymentService->createPaymentIntent(
            $booking,
            $data['type'],
            $amount,
            $request->user()->id
        );

        return $this->success($intent);
    }

    public function confirmPayment(Request $request, Booking $booking): JsonResponse
    {
        $this->authorize('view', $booking);

        if ($booking->user_id !== $request->user()->id) {
            abort(403);
        }

        $data = $request->validate([
            'payment_intent_id' => ['required', 'string'],
        ]);

        $this->paymentService->confirmPayment($booking, $data['payment_intent_id']);

        return $this->success(
            new BookingResource($booking->fresh(['parkingSpace', 'parkingSlot'])),
            'Payment confirmed.'
        );
    }
}
