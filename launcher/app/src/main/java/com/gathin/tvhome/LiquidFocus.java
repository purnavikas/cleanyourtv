package com.gathin.tvhome;

import android.view.View;
import android.widget.TextView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/** Springs retain velocity when D-pad focus changes direction, rather than restarting a timed zoom. */
final class LiquidFocus {
    private final View card, artwork;
    private final TextView label;
    private final float density, restAlpha;
    private final boolean advanced;
    private final SpringAnimation scaleX, scaleY, lift;

    LiquidFocus(View card, View artwork, TextView label, float restAlpha, boolean advanced) {
        this.card = card;
        this.artwork = artwork;
        this.label = label;
        this.restAlpha = restAlpha;
        this.advanced = advanced;
        density = card.getResources().getDisplayMetrics().density;
        scaleX = spring(card, DynamicAnimation.SCALE_X, 0.001f);
        scaleY = spring(card, DynamicAnimation.SCALE_Y, 0.001f);
        lift = spring(card, DynamicAnimation.TRANSLATION_Y, 0.2f);
        card.setCameraDistance(8000f * density);
    }

    private static SpringAnimation spring(View view, DynamicAnimation.ViewProperty property, float threshold) {
        SpringAnimation animation = new SpringAnimation(view, property);
        animation.setSpring(new SpringForce().setStiffness(LiquidStyle.FOCUS_STIFFNESS)
                .setDampingRatio(LiquidStyle.FOCUS_DAMPING));
        animation.setMinimumVisibleChange(threshold);
        return animation;
    }

    void focus(boolean focused) {
        float scale = focused ? LiquidStyle.FOCUS_SCALE : 1f;
        scaleX.animateToFinalPosition(scale);
        scaleY.animateToFinalPosition(scale);
        lift.animateToFinalPosition(0f);
        card.setTranslationZ(focused && advanced ? 10f * density : 0f);
        artwork.animate().scaleX(focused && advanced ? 1.045f : 1f).scaleY(focused && advanced ? 1.045f : 1f)
                .translationX(0).translationY(0).setDuration(420).setInterpolator(LiquidStyle.EASE).start();
        label.animate().alpha(focused ? 1f : restAlpha).translationY(focused ? 4f * density : -6f * density)
                .setDuration(focused ? 300 : 240).setInterpolator(LiquidStyle.EASE).start();
    }

    /** Remote press compresses the focused tile; release returns to its focus spring. */
    void press(boolean pressed) {
        float target = pressed ? 1.04f : card.isFocused() ? LiquidStyle.FOCUS_SCALE : 1f;
        scaleX.animateToFinalPosition(target);
        scaleY.animateToFinalPosition(target);
        lift.animateToFinalPosition(0f);
    }

    /** RecyclerView can rebind a holder mid-animation; no old spring may resize a newly bound tile. */
    void snap(boolean focused) {
        scaleX.cancel();
        scaleY.cancel();
        lift.cancel();
        card.setScaleX(focused ? LiquidStyle.FOCUS_SCALE : 1f);
        card.setScaleY(focused ? LiquidStyle.FOCUS_SCALE : 1f);
        card.setTranslationY(0f);
        artwork.setTranslationX(0);
        artwork.setTranslationY(0);
    }

}
