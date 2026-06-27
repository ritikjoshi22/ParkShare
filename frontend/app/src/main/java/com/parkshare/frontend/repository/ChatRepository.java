package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.MessageDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository extends BaseRepository {
    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getConversations(RepositoryCallback<List<MessageDto>> callback) {
        enqueue(api.conversations(), callback);
    }

    public void getMessages(long bookingId, RepositoryCallback<List<MessageDto>> callback) {
        enqueue(api.messages(bookingId), callback);
    }

    public void sendMessage(long receiverId, Long bookingId, String message, RepositoryCallback<MessageDto> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("receiver_id", receiverId);
        if (bookingId != null) {
            body.put("booking_id", bookingId);
        }
        body.put("message", message);
        enqueue(api.sendMessage(body), callback);
    }
}
