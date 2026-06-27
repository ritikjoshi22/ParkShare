package com.parkshare.frontend.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class DateTimeFormatUtil {

    private static final DateTimeFormatter DISPLAY =
            DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a", Locale.getDefault());

    private DateTimeFormatUtil() {
    }

    public static String formatBookingRange(String startIso, String endIso) {
        try {
            OffsetDateTime start = OffsetDateTime.parse(startIso);
            OffsetDateTime end = endIso != null ? OffsetDateTime.parse(endIso) : null;
            if (end != null) {
                return start.format(DISPLAY) + " – " + end.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()));
            }
            return start.format(DISPLAY);
        } catch (DateTimeParseException e) {
            return startIso != null ? startIso : "";
        }
    }

    public static String formatTimeOnly(String iso) {
        try {
            if (iso == null) return "";
            OffsetDateTime dt = OffsetDateTime.parse(iso);
            return dt.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()));
        } catch (DateTimeParseException e) {
            return iso != null ? iso : "";
        }
    }

    public static String toApiIso(OffsetDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
