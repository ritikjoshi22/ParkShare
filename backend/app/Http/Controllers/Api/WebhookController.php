<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\BookingPayment;
use App\Services\PaymentService;
use App\Services\StripeService;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Log;
use Stripe\Exception\SignatureVerificationException;

class WebhookController extends Controller
{
    public function __construct(
        protected StripeService $stripeService,
        protected PaymentService $paymentService
    ) {}

    public function handleStripe(Request $request)
    {
        $payload = $request->getContent();
        $sigHeader = $request->header('Stripe-Signature');

        try {
            $event = $this->stripeService->constructWebhookEvent($payload, $sigHeader);
        } catch (\UnexpectedValueException $e) {
            return response()->json(['error' => 'Invalid payload'], 400);
        } catch (SignatureVerificationException $e) {
            return response()->json(['error' => 'Invalid signature'], 400);
        }

        Log::info('Stripe Webhook received', ['type' => $event->type]);

        switch ($event->type) {
            case 'payment_intent.succeeded':
                $this->handlePaymentIntentSucceeded($event->data->object);
                break;
            case 'payment_intent.payment_failed':
                $this->handlePaymentIntentFailed($event->data->object);
                break;
            // Add more cases as needed (e.g. charge.refunded)
        }

        return response()->json(['status' => 'success']);
    }

    protected function handlePaymentIntentSucceeded($intent)
    {
        $paymentId = $intent->metadata->payment_id ?? null;
        if (!$paymentId) return;

        $payment = BookingPayment::find($paymentId);
        if ($payment && $payment->payment_status !== 'paid') {
            $this->paymentService->markAsPaid($payment, $intent->latest_charge, [
                'intent_id' => $intent->id,
                'amount' => $intent->amount / 100,
                'currency' => $intent->currency,
                'method' => 'webhook'
            ]);
            Log::info('Payment marked as paid via Webhook', ['payment_id' => $paymentId]);
        }
    }

    protected function handlePaymentIntentFailed($intent)
    {
        $paymentId = $intent->metadata->payment_id ?? null;
        if (!$paymentId) return;

        $payment = BookingPayment::find($paymentId);
        if ($payment) {
            $payment->update(['payment_status' => 'failed']);
            Log::warning('Payment marked as failed via Webhook', ['payment_id' => $paymentId, 'error' => $intent->last_payment_error?->message]);
        }
    }
}
