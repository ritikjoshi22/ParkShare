package com.parkshare.frontend.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.parkshare.frontend.R;

/**
 * Darkens the camera preview outside a rounded scan window.
 */
public class ScanOverlayView extends View {

    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint clearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF scanRect = new RectF();
    private final Path borderPath = new Path();
    private float cornerRadius;

    public ScanOverlayView(Context context) {
        super(context);
        init(context);
    }

    public ScanOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        dimPaint.setColor(0xB3000000);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8f);
        borderPaint.setColor(ContextCompat.getColor(context, R.color.primary));
        cornerRadius = 24f * context.getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float size = Math.min(w, h) * 0.72f;
        float left = (w - size) / 2f;
        float top = (h - size) / 2f - h * 0.06f;
        scanRect.set(left, top, left + size, top + size);
        borderPath.reset();
        borderPath.addRoundRect(scanRect, cornerRadius, cornerRadius, Path.Direction.CW);
    }

    public RectF getScanRect() {
        return scanRect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int save = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        canvas.drawRect(0, 0, getWidth(), getHeight(), dimPaint);
        canvas.drawRoundRect(scanRect, cornerRadius, cornerRadius, clearPaint);
        canvas.restoreToCount(save);
        canvas.drawPath(borderPath, borderPaint);
    }
}
