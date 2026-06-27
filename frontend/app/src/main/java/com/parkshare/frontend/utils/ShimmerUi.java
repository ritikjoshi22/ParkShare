package com.parkshare.frontend.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.parkshare.frontend.R;

public final class ShimmerUi {

    private ShimmerUi() {
    }

    public static void prepareListSkeleton(View root, @LayoutRes int itemLayoutRes, int count) {
        ShimmerFrameLayout shimmer = root.findViewById(R.id.shimmerLayout);
        LinearLayout container = root.findViewById(R.id.shimmerContainer);
        if (shimmer == null || container == null) {
            return;
        }
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        for (int i = 0; i < count; i++) {
            container.addView(inflater.inflate(itemLayoutRes, container, false));
        }
    }
}
