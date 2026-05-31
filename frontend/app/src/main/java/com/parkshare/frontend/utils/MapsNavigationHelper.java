package com.parkshare.frontend.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.parkshare.frontend.R;

/**
 * Opens turn-by-turn navigation in Google Maps (app or browser).
 * In-app map display uses OpenStreetMap (osmdroid); this helper is only for external navigation.
 */
public final class MapsNavigationHelper {

    private static final String GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps";

    private MapsNavigationHelper() {
    }

    public static void openNavigation(Context context, double latitude, double longitude,
                                      @Nullable String label) {
        if (tryGoogleMapsNavigation(context, latitude, longitude)) {
            return;
        }
        if (tryGoogleMapsDirections(context, latitude, longitude)) {
            return;
        }
        if (tryWebDirections(context, latitude, longitude)) {
            return;
        }
        if (tryGeoUri(context, latitude, longitude, label)) {
            return;
        }
        Toast.makeText(context, R.string.no_maps_app, Toast.LENGTH_LONG).show();
    }

    private static boolean tryGoogleMapsNavigation(Context context, double lat, double lng) {
        try {
            Uri uri = Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=d");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(GOOGLE_MAPS_PACKAGE);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ignored) {
            return false;
        }
    }

    private static boolean tryGoogleMapsDirections(Context context, double lat, double lng) {
        try {
            Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(GOOGLE_MAPS_PACKAGE);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException ignored) {
            return false;
        }
    }

    private static boolean tryWebDirections(Context context, double lat, double lng) {
        Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        context.startActivity(intent);
        return true;
    }

    private static boolean tryGeoUri(Context context, double lat, double lng, @Nullable String label) {
        String q = lat + "," + lng;
        if (label != null && !label.isEmpty()) {
            q += "(" + Uri.encode(label) + ")";
        }
        Uri uri = Uri.parse("geo:" + lat + "," + lng + "?q=" + q);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        context.startActivity(intent);
        return true;
    }
}
