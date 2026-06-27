package com.parkshare.frontend.utils;

import com.parkshare.api.models.ParkingImageDto;
import com.parkshare.api.models.ParkingSpaceDto;
import com.parkshare.frontend.models.Parking;

import java.util.List;
import java.util.Locale;

public final class ParkingMapper {

    private ParkingMapper() {
    }

    public static Parking fromDto(ParkingSpaceDto dto) {
        String imageUrl = "";
        List<ParkingImageDto> images = dto.getImages();
        if (images != null && !images.isEmpty() && images.get(0).getImageUrl() != null) {
            imageUrl = images.get(0).getImageUrl();
        }

        double rating = dto.getReviewsAvgRating() != null ? dto.getReviewsAvgRating() : 0.0;
        String hours = formatHours(dto.getOpeningTime(), dto.getClosingTime());
        String distance = null;
        if (dto.getDistanceKm() != null) {
            distance = String.format(Locale.getDefault(), "%.1f km away", dto.getDistanceKm());
        }

        Parking parking = new Parking(
                String.valueOf(dto.getId()),
                dto.getParkingName(),
                dto.getAddress(),
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getPricePerHour(),
                (int) dto.getTotalSlots(),
                (int) dto.getAvailableSlots(),
                rating,
                imageUrl,
                dto.getDescription() != null ? dto.getDescription() : "",
                hours,
                isOpenNow(dto.getOpeningTime(), dto.getClosingTime())
        );
        parking.setDistance(distance);
        return parking;
    }

    private static String formatHours(String open, String close) {
        if (open == null || close == null) {
            return "Hours unavailable";
        }
        return open.substring(0, Math.min(5, open.length()))
                + " - "
                + close.substring(0, Math.min(5, close.length()));
    }

    private static boolean isOpenNow(String open, String close) {
        return open != null && close != null;
    }
}
