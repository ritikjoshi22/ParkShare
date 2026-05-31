package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.NotificationDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.List;

public class NotificationRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getNotifications(int page, boolean unreadOnly, RepositoryCallback<List<NotificationDto>> callback) {
        enqueue(api.notifications(page, 20, unreadOnly), callback);
    }

    public void markRead(long id, RepositoryCallback<NotificationDto> callback) {
        enqueue(api.markNotificationRead(id), callback);
    }

    public void markAllRead(RepositoryCallback<Void> callback) {
        enqueueVoid(api.markAllNotificationsRead(), callback);
    }
}
