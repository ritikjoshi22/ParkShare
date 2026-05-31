package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.ReviewDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getForParking(long parkingSpaceId, RepositoryCallback<List<ReviewDto>> callback) {
        enqueue(api.reviews(parkingSpaceId, null, 1, 20), callback);
    }

    public void submitParkingReview(long parkingSpaceId, int rating, String text,
                                    RepositoryCallback<ReviewDto> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("parking_space_id", parkingSpaceId);
        body.put("rating", rating);
        if (text != null && !text.isEmpty()) {
            body.put("review_text", text);
        }
        enqueue(api.createReview(body), callback);
    }
}
