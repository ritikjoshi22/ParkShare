<?php

namespace App\Services;

use Stripe\StripeClient;
use Stripe\Exception\ApiErrorException;
use Illuminate\Support\Facades\Log;

class StripeService
{
    protected ?StripeClient $client = null;

    public function __construct()
    {
        $secret = config('services.stripe.secret');
        if ($secret) {
            $this->client = new StripeClient($secret);
        }
    }

    public function getClient(): ?StripeClient
    {
        return $this->client;
    }

    public function isEnabled(): bool
    {
        return $this->client !== null;
    }

    /**
     * @param array<string, mixed> $params
     */
    public function createPaymentIntent(array $params)
    {
        if (!$this->client) {
            throw new \Exception('Stripe is not configured.');
        }

        try {
            return $this->client->paymentIntents->create($params);
        } catch (ApiErrorException $e) {
            Log::error('Stripe PaymentIntent Creation Failed', [
                'error' => $e->getMessage(),
                'params' => $params
            ]);
            throw $e;
        }
    }

    public function retrievePaymentIntent(string $id)
    {
        if (!$this->client) {
            throw new \Exception('Stripe is not configured.');
        }

        return $this->client->paymentIntents->retrieve($id);
    }

    public function constructWebhookEvent(string $payload, string $sigHeader)
    {
        if (!$this->client) {
            throw new \Exception('Stripe is not configured.');
        }

        $secret = config('services.stripe.webhook_secret');

        return \Stripe\Webhook::constructEvent(
            $payload,
            $sigHeader,
            $secret
        );
    }
}
