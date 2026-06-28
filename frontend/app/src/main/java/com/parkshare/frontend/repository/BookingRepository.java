package com.parkshare.frontend.repository;

import com.parkshare.api.ApiClient;
import com.parkshare.api.ApiService;
import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.BookingQuoteDto;
import com.parkshare.api.models.ExtensionOptionsDto;
import com.parkshare.api.models.ParkingSlotDto;
import com.parkshare.api.models.PaymentIntentDto;
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

    public void getSlots(long parkingId, String startTime, String endTime,
                         RepositoryCallback<List<ParkingSlotDto>> callback) {
        enqueue(api.parkingSlots(parkingId, startTime, endTime), callback);
    }

    public void createBooking(long parkingSpaceId, Long slotId, String startTime, String endTime,
                              RepositoryCallback<BookingDto> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("parking_space_id", parkingSpaceId);
        body.put("start_time", startTime);
        body.put("end_time", endTime);
        if (slotId != null) {
            body.put("parking_slot_id", slotId);
        }
        enqueue(api.createBooking(body), callback);
    }

    public void cancelBooking(long bookingId, RepositoryCallback<BookingDto> callback) {
        enqueue(api.cancelBooking(bookingId), callback);
    }

    public void getBooking(long bookingId, RepositoryCallback<BookingDto> callback) {
        enqueue(api.booking(bookingId), callback);
    }

    public void scanBookingQr(String qrPayload, RepositoryCallback<com.parkshare.api.models.BookingScanResultDto> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("qr_payload", qrPayload);
        enqueue(api.scanBookingQr(body), callback);
    }

    public void quoteBooking(long parkingId, Long slotId, String startTime, String endTime,
                             RepositoryCallback<BookingQuoteDto> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("parking_space_id", parkingId);
        body.put("start_time", startTime);
        body.put("end_time", endTime);
        if (slotId != null) {
            body.put("parking_slot_id", slotId);
        }
        enqueue(api.quoteBooking(body), callback);
    }

    public void getActiveBooking(RepositoryCallback<BookingDto> callback) {
        enqueue(api.activeBooking(), callback);
    }

    public void getExtensionOptions(long bookingId, RepositoryCallback<ExtensionOptionsDto> callback) {
        enqueue(api.extensionOptions(bookingId), callback);
    }

    public void extendBooking(long bookingId, int minutes, RepositoryCallback<BookingDto> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("minutes", minutes);
        enqueue(api.extendBooking(bookingId, body), callback);
    }

    public void createPaymentIntent(long bookingId, String type, RepositoryCallback<PaymentIntentDto> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("type", type);
        enqueue(api.createPaymentIntent(bookingId, body), callback);
    }

    public void confirmPayment(long bookingId, String paymentIntentId, RepositoryCallback<BookingDto> callback) {
        Map<String, String> body = new HashMap<>();
        body.put("payment_intent_id", paymentIntentId);
        enqueue(api.confirmPayment(bookingId, body), callback);
    }
}
