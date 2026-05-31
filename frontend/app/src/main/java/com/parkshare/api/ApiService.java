package com.parkshare.api;

import com.parkshare.api.models.ApiResponse;
import com.parkshare.api.models.AuthData;
import com.parkshare.api.models.BookingDto;
import com.parkshare.api.models.FavoriteDto;
import com.parkshare.api.models.NotificationDto;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.api.models.ReviewDto;
import com.parkshare.api.models.SosRequestDto;
import com.parkshare.api.models.TechnicianDto;
import com.parkshare.api.models.UserDto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("auth/login")
    Call<ApiResponse<AuthData>> login(@Body Map<String, String> body);

    @POST("auth/register")
    Call<ApiResponse<AuthData>> register(@Body Map<String, Object> body);

    @POST("auth/logout")
    Call<ApiResponse<Void>> logout();

    @GET("auth/profile")
    Call<ApiResponse<UserDto>> profile();

    @GET("parking-spaces/nearby")
    Call<ApiResponse<List<ParkingSpaceDto>>> nearbyParking(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radius_km") double radiusKm,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @GET("parking-spaces")
    Call<ApiResponse<List<ParkingSpaceDto>>> parkingSpaces(
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("latitude") Double latitude,
            @Query("longitude") Double longitude
    );

    @GET("parking-spaces/{id}")
    Call<ApiResponse<ParkingSpaceDto>> parkingSpace(@Path("id") long id);

    @GET("bookings")
    Call<ApiResponse<List<BookingDto>>> bookings(
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("status") String status
    );

    @GET("bookings/history")
    Call<ApiResponse<List<BookingDto>>> bookingHistory(
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @POST("bookings")
    Call<ApiResponse<BookingDto>> createBooking(@Body Map<String, Object> body);

    @POST("bookings/{id}/cancel")
    Call<ApiResponse<BookingDto>> cancelBooking(@Path("id") long id);

    @GET("sos-requests")
    Call<ApiResponse<List<SosRequestDto>>> sosRequests(
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("status") String status
    );

    @POST("sos-requests")
    Call<ApiResponse<SosRequestDto>> createSos(@Body Map<String, Object> body);

    @PATCH("sos-requests/{id}/status")
    Call<ApiResponse<SosRequestDto>> updateSosStatus(
            @Path("id") long id,
            @Body Map<String, String> body
    );

    @GET("technicians/profile")
    Call<ApiResponse<TechnicianDto>> technicianProfile();

    @GET("technicians")
    Call<ApiResponse<List<TechnicianDto>>> technicians(
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("status") String status
    );

    @GET("reviews")
    Call<ApiResponse<List<ReviewDto>>> reviews(
            @Query("parking_space_id") Long parkingSpaceId,
            @Query("technician_id") Long technicianId,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @POST("reviews")
    Call<ApiResponse<ReviewDto>> createReview(@Body Map<String, Object> body);

    @GET("favorites")
    Call<ApiResponse<List<FavoriteDto>>> favorites(
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @POST("favorites")
    Call<ApiResponse<FavoriteDto>> addFavorite(@Body Map<String, Long> body);

    @DELETE("favorites/{id}")
    Call<ApiResponse<Void>> removeFavorite(@Path("id") long favoriteId);

    @GET("notifications")
    Call<ApiResponse<List<NotificationDto>>> notifications(
            @Query("page") int page,
            @Query("per_page") int perPage,
            @Query("unread_only") boolean unreadOnly
    );

    @PATCH("notifications/{id}/read")
    Call<ApiResponse<NotificationDto>> markNotificationRead(@Path("id") long id);

    @POST("notifications/read-all")
    Call<ApiResponse<Void>> markAllNotificationsRead();
}
