package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/** Translucent optical shell. Shaders are rebuilt only when bounds or focus changes, never on a frame. */
final class LiquidSurface extends Drawable {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint light = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private final boolean pill;
    private boolean focused;
    private int opacity = 255;
    private final Paint backdrop = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final android.graphics.Matrix backdropMatrix = new android.graphics.Matrix();
    private final int[] screenLocation = new int[2];
    private android.graphics.Bitmap sampled;

    LiquidSurface(float density, boolean pill) {
        this.density = density;
        this.pill = pill;
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(density * 0.8f);
        light.setStyle(Paint.Style.STROKE);
        light.setStrokeWidth(density * 0.7f);
    }

    @Override protected void onBoundsChange(Rect bounds) {
        rect.set(bounds.left + density, bounds.top + density, bounds.right - density, bounds.bottom - density);
        updateShaders();
    }

    private void updateShaders() {
        if (rect.height() <= 0) return;
        fill.setShader(new LinearGradient(rect.left, rect.top, rect.right * 0.7f, rect.bottom,
                focused ? new int[]{0xF2FFFFFF, 0xD4EAF2FF, 0xC9FFFFFF}
                        : new int[]{0x38E5EEFF, 0x20FFFFFF, 0x16FFFFFF},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        rim.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom,
                new int[]{0xB3FFFFFF, 0x14FFFFFF, 0x66C9E0FF, 0x80FFFFFF},
                new float[]{0f, 0.42f, 0.7f, 1f}, Shader.TileMode.CLAMP));
        light.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.top,
                new int[]{0x00FFFFFF, 0xCCFFFFFF, 0x00FFFFFF}, null, Shader.TileMode.CLAMP));
    }

    @Override public boolean isStateful() { return true; }

    @Override protected boolean onStateChange(int[] states) {
        boolean next = false;
        for (int state : states) if (state == android.R.attr.state_focused || state == android.R.attr.state_pressed) next = true;
        if (next == focused) return false;
        focused = next;
        updateShaders();
        invalidateSelf();
        return true;
    }

    @Override public void draw(Canvas canvas) {
        float radius = pill ? rect.height() * 0.5f : 26f * density;
        if (getCallback() instanceof android.view.View) {
            android.view.View view = (android.view.View) getCallback();
            if (view.getContext() instanceof MainActivity) {
                android.graphics.Bitmap bitmap = ((MainActivity) view.getContext()).glassBackdrop();
                if (bitmap != null && !bitmap.isRecycled()) {
                    if (sampled != bitmap) {
                        sampled = bitmap;
                        backdrop.setShader(new android.graphics.BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                    }
                    view.getLocationOnScreen(screenLocation);
                    android.util.DisplayMetrics metrics = view.getResources().getDisplayMetrics();
                    backdropMatrix.setScale(metrics.widthPixels / (float) bitmap.getWidth(),
                            metrics.heightPixels / (float) bitmap.getHeight());
                    backdropMatrix.postTranslate(-screenLocation[0], -screenLocation[1]);
                    backdrop.getShader().setLocalMatrix(backdropMatrix);
                    backdrop.setAlpha(opacity);
                    canvas.drawRoundRect(rect, radius, radius, backdrop);
                }
            }
        }
        fill.setAlpha(opacity);
        rim.setAlpha(opacity);
        light.setAlpha(opacity);
        canvas.drawRoundRect(rect, radius, radius, fill);
        canvas.drawRoundRect(rect, radius, radius, rim);
        canvas.save();
        canvas.clipRect(rect.left, rect.top, rect.right, rect.top + 2f * density);
        canvas.drawRoundRect(rect, radius, radius, light);
        canvas.restore();
    }

    @Override public void setAlpha(int alpha) { opacity = alpha; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { fill.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
