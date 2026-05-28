<?php

namespace App\Http\Controllers\Api;

use App\Http\Resources\NotificationResource;
use App\Models\Notification;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class NotificationController extends ApiController
{
    public function index(Request $request): JsonResponse
    {
        $notifications = $request->user()
            ->notifications()
            ->when($request->boolean('unread_only'), fn ($q) => $q->where('is_read', false))
            ->latest()
            ->paginate($request->integer('per_page', 20));

        return $this->success(NotificationResource::collection($notifications));
    }

    public function markAsRead(Notification $notification): JsonResponse
    {
        $this->authorize('update', $notification);

        $notification->update(['is_read' => true]);

        return $this->success(new NotificationResource($notification), 'Notification marked as read.');
    }

    public function markAllAsRead(Request $request): JsonResponse
    {
        $request->user()->notifications()->where('is_read', false)->update(['is_read' => true]);

        return $this->success(null, 'All notifications marked as read.');
    }
}
