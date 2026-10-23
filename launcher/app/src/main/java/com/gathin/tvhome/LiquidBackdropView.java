package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/** Two tiny, pre-softened textures crossfade without a fade-through-black or full-screen blur buffer. */
public class LiquidBackdropView extends ImageView {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF bounds = new RectF();
    private Bitmap previous, current;
    private float blend = 1f;
    private ValueAnimator transition;
    private boolean motion = true;

    public LiquidBackdropView(Context context, AttributeSet attrs) { super(context, attrs); }

    void setMotion(boolean enabled) { motion = enabled; }

    @Override public void setImageBitmap(Bitmap bitmap) {
        if (!LiquidStyle.enabled(getContext())) { super.setImageBitmap(bitmap); return; }
        if (bitmap == current) return;
        // Keep the dominant image when fast navigation retargets an unfinished blend.
        if (blend >= 0.5f || previous == null) previous = current;
        if (transition != null) transition.cancel();
        current = bitmap;
        if (!motion || previous == null || bitmap == null) { blend = 1f; invalidate(); return; }
        transition = ValueAnimator.ofFloat(0f, 1f);
        transition.setDuration(320);
        transition.setInterpolator(LiquidStyle.EASE);
        transition.addUpdateListener(a -> { blend = (float) a.getAnimatedValue(); invalidate(); });
        transition.start();
    }

    @Override protected void onDraw(Canvas canvas) {
        if (!LiquidStyle.enabled(getContext())) { super.onDraw(canvas); return; }
        if (previous != null && !previous.isRecycled() && blend < 1f) {
            paint.setAlpha(255);
            drawCover(canvas, previous);
        }
        if (current != null && !current.isRecycled()) {
            paint.setAlpha(Math.round(255f * blend));
            drawCover(canvas, current);
        }
    }

    private void drawCover(Canvas canvas, Bitmap bitmap) {
        float scale = Math.max(getWidth() / (float) bitmap.getWidth(), getHeight() / (float) bitmap.getHeight());
        float w = bitmap.getWidth() * scale, h = bitmap.getHeight() * scale;
        float x = (getWidth() - w) * 0.5f, y = (getHeight() - h) * 0.5f;
        bounds.set(x, y, x + w, y + h);
        canvas.drawBitmap(bitmap, null, bounds, paint);
    }

    @Override protected void onDetachedFromWindow() {
        if (transition != null) transition.cancel();
        super.onDetachedFromWindow();
    }

    @Override public boolean hasOverlappingRendering() { return false; }
}
