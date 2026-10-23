package com.gathin.tvhome;

import android.content.Context;
import android.view.animation.PathInterpolator;
import android.view.animation.Interpolator;

/** Local visual treatment. No Home, package, permission or input routing changes. */
final class LiquidStyle {
    static final float FOCUS_SCALE = 306f / 250f;
    static final float FOCUS_STIFFNESS = 280f;
    static final float FOCUS_DAMPING = 0.88f;
    static final Interpolator EASE = new PathInterpolator(0.25f, 0.1f, 0.25f, 1f);

    private LiquidStyle() { }

    static boolean enabled(Context context) {
        return context.getResources().getBoolean(R.bool.liquid_glass);
    }

    static float radius(float width, float density) {
        return width * (30f / 250f);
    }
}
