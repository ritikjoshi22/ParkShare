<?php

namespace App\Http\Controllers\Api;

use App\Events\MessageSent;
use App\Models\Booking;
use App\Models\Message;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ChatController extends ApiController
{
    public function index(Request $request, ?Booking $booking = null): JsonResponse
    {
        $userId = $request->user()->id;

        $query = Message::query()
            ->where(function ($q) use ($userId) {
                $q->where('sender_id', $userId)
                  ->orWhere('receiver_id', $userId);
            });

        if ($booking) {
            $query->where('booking_id', $booking->id);
        }

        $messages = $query->oldest()->get();

        // Mark as read
        Message::where('receiver_id', $userId)
            ->where('is_read', false)
            ->when($booking, fn($q) => $q->where('booking_id', $booking->id))
            ->update(['is_read' => true]);

        return $this->success($messages);
    }

    public function sendMessage(Request $request): JsonResponse
    {
        $data = $request->validate([
            'receiver_id' => 'required|exists:users,id',
            'booking_id' => 'nullable|exists:bookings,id',
            'message' => 'required|string|max:1000',
        ]);

        $message = Message::create([
            'sender_id' => $request->user()->id,
            'receiver_id' => $data['receiver_id'],
            'booking_id' => $data['booking_id'] ?? null,
            'message' => $data['message'],
        ]);

        broadcast(new MessageSent($message))->toOthers();

        return $this->success($message, 'Message sent.', 201);
    }

    public function getConversations(Request $request): JsonResponse
    {
        $userId = $request->user()->id;

        // This is a simplified conversation list
        $conversations = Message::where('sender_id', $userId)
            ->orWhere('receiver_id', $userId)
            ->with(['sender', 'receiver'])
            ->latest()
            ->get()
            ->groupBy(function($msg) use ($userId) {
                return $msg->sender_id == $userId ? $msg->receiver_id : $msg->sender_id;
            })
            ->map(function($msgs) {
                return $msg = $msgs->first();
            })
            ->values();

        return $this->success($conversations);
    }
}
