<?php

namespace App\Services;

use App\Models\Booking;
use App\Models\BookingPayment;
use Illuminate\Support\Facades\Log;
use Illuminate\Validation\ValidationException;
use Stripe\Exception\ApiErrorException;

class PaymentService
{
    public function __construct(
        protected StripeService $stripeService,
        protected SystemSettingsService $settings
    ) {}

    /**
     * @return array{payment_intent_id: string, client_secret: string, amount: float, currency: string, payment_id: int}
     */
    public function createPaymentIntent(Booking $booking, string $type, float $amount, int $userId): array
    {
        if ($amount <= 0) {
            throw ValidationException::withMessages([
                'amount' => ['Nothing to pay.'],
            ]);
        }

        $currency = strtolower((string) $this->settings->get('stripe_currency', 'usd'));

        $payment = BookingPayment::create([
            'booking_id' => $booking->id,
            'user_id' => $userId,
            'type' => $type,
            'amount' => $amount,
            'currency' => $currency,
            'payment_status' => 'pending',
            'metadata' => ['booking_id' => $booking->id, 'type' => $type],
        ]);

        if (!$this->stripeService->isEnabled()) {
            // Dev mode / fallback without Stripe keys
            $this->markAsPaid($payment, 'dev_bypass', ['mode' => 'dev_skip']);

            return [
                'payment_intent_id' => 'dev_'.$payment->id,
                'client_secret' => '',
                'amount' => $amount,
                'currency' => $currency,
                'payment_id' => $payment->id,
                'dev_mode' => true,
            ];
        }

        try {
            $intent = $this->stripeService->createPaymentIntent([
                'amount' => (int) round($amount * 100),
                'currency' => $currency,
                'metadata' => [
                    'booking_id' => (string) $booking->id,
                    'payment_id' => (string) $payment->id,
                    'type' => $type,
                ],
                'automatic_payment_methods' => ['enabled' => true],
            ]);

            $payment->update(['stripe_payment_intent_id' => $intent->id]);

            return [
                'payment_intent_id' => $intent->id,
                'client_secret' => $intent->client_secret,
                'amount' => $amount,
                'currency' => $currency,
                'payment_id' => $payment->id,
                'dev_mode' => false,
                'publishable_key' => config('services.stripe.key'),
            ];
        } catch (ApiErrorException $e) {
            $payment->update(['payment_status' => 'failed']);
            throw ValidationException::withMessages([
                'payment' => ['Payment could not be initiated: ' . $e->getMessage()],
            ]);
        }
    }

    public function confirmPayment(Booking $booking, string $paymentIntentId): BookingPayment
    {
        $payment = BookingPayment::query()
            ->where('booking_id', $booking->id)
            ->where('stripe_payment_intent_id', $paymentIntentId)
            ->firstOrFail();

        if ($payment->payment_status === 'paid') {
            return $payment;
        }

        if (!$this->stripeService->isEnabled()) {
            return $payment;
        }

        try {
            $intent = $this->stripeService->retrievePaymentIntent($paymentIntentId);

            if ($intent->status === 'succeeded') {
                $this->markAsPaid($payment, $intent->latest_charge, [
                    'intent_id' => $intent->id,
                    'amount' => $intent->amount / 100,
                    'currency' => $intent->currency,
                    'status' => $intent->status
                ]);
            } else {
                throw ValidationException::withMessages([
                    'payment' => ['Payment has not completed yet. Status: '.$intent->status],
                ]);
            }
        } catch (ApiErrorException $e) {
            throw ValidationException::withMessages([
                'payment' => ['Error verifying payment: ' . $e->getMessage()],
            ]);
        }

        return $payment->fresh();
    }

    public function markAsPaid(BookingPayment $payment, string $chargeId, array $receipt): void
    {
        if ($payment->payment_status === 'paid') {
            return;
        }

        $payment->update([
            'payment_status' => 'paid',
            'stripe_charge_id' => $chargeId,
            'receipt' => $receipt,
            'paid_at' => now(),
        ]);

        $this->applySuccessfulPayment($payment->booking, $payment);
    }

    protected function applySuccessfulPayment(Booking $booking, BookingPayment $payment): void
    {
        $newDue = round(max(0, (float) $booking->amount_due - (float) $payment->amount), 2);
        $status = $newDue <= 0 ? 'paid' : 'partial';

        $booking->update([
            'amount_due' => $newDue,
            'payment_status' => $status,
        ]);

        // If it's a booking payment and now fully paid, we can confirm the booking if it was pending
        if (($payment->type === 'booking' || $payment->type === 'balance')
            && $booking->booking_status === 'pending'
            && $status === 'paid') {
            $booking->update(['booking_status' => 'confirmed']);
            app(\App\Services\BookingQrService::class)->syncStoredQr($booking);
        }
    }

    public function assertCheckoutAllowed(Booking $booking): void
    {
        if ((float) $booking->amount_due > 0) {
            throw ValidationException::withMessages([
                'payment' => [
                    'Outstanding balance exists. Amount due: NPR '.number_format((float) $booking->amount_due, 2),
                ],
            ]);
        }
    }
}
