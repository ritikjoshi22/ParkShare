package com.parkshare.frontend.utils;

import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.facebook.shimmer.ShimmerFrameLayout;

public final class LoadingHelper {

    private LoadingHelper() {
    }

    public static void showShimmer(@Nullable ShimmerFrameLayout shimmer,
                                   @Nullable ProgressBar progressBar) {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (shimmer != null) {
            shimmer.setVisibility(View.VISIBLE);
            shimmer.startShimmer();
        }
    }

    public static void showProgress(@Nullable ShimmerFrameLayout shimmer,
                                    @Nullable ProgressBar progressBar) {
        hideShimmer(shimmer);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public static void hideShimmer(@Nullable ShimmerFrameLayout shimmer) {
        if (shimmer != null) {
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
        }
    }

    public static void hideAll(@Nullable ShimmerFrameLayout shimmer,
                             @Nullable ProgressBar progressBar) {
        hideShimmer(shimmer);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
