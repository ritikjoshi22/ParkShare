package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.BookingDto;
import com.parkshare.frontend.utils.RepositoryCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingRepository extends BaseRepository {

    private final ApiService api = ApiClient.getInstance().getApiService();

    public void getBookings(int page, String status, RepositoryCallback<List<BookingDto>> callback) {
        enqueue(api.bookings(page, 15, status), callback);
    }

    public void getHistory(int page, RepositoryCallback<List<BookingDto>> callback) {
        enqueue(api.bookingHistory(page, 15), callback);
    }

    public void createBooking(long parkingSpaceId, String startTime, String endTime,
                              RepositoryCallback<BookingDto> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("parking_space_id", parkingSpaceId);
        body.put("start_time", startTime);
        body.put("end_time", endTime);
        enqueue(api.createBooking(body), callback);
    }

    public void cancelBooking(long bookingId, RepositoryCallback<BookingDto> callback) {
        enqueue(api.cancelBooking(bookingId), callback);
    }
}
