package com.gathin.tvhome;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextClock;
import android.widget.TextView;

/** Compact floating controls over the wallpaper, with a separate profile tab. */
final class LiquidControlCenter {
    private final Context context;
    private final float unit, density;
    private final boolean motion;
    private final PopupWindow popup;
    private final LinearLayout panel;
    private final ColorStateList ink = new ColorStateList(new int[][]{
            {android.R.attr.state_focused}, {}}, new int[]{0xFF16161A, Color.WHITE});

    LiquidControlCenter(Context context, String network, boolean motion, Runnable profile, Runnable[] actions) {
        this.context = context;
        this.motion = motion;
        density = context.getResources().getDisplayMetrics().density;
        unit = context.getResources().getDisplayMetrics().widthPixels / 1920f;
        panel = column();
        panel.setClipChildren(false);
        panel.setClipToPadding(false);
        popup = new PopupWindow(panel, px(440), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(0);
        popup.setAnimationStyle(0);

        LinearLayout tabs = row();
        tabs.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        TextClock clock = new TextClock(context);
        clock.setFormat12Hour("h:mma");
        clock.setFormat24Hour("HH:mm");
        clock.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(26));
        clock.setTextColor(Color.WHITE);
        clock.setIncludeFontPadding(false);
        tabs.addView(clock, new LinearLayout.LayoutParams(0, px(60), 1));
        clock.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        View controls = tile(R.drawable.ic_cc_controls, context.getString(R.string.cc_controls), true, true, null);
        android.graphics.drawable.GradientDrawable selectedTab = new android.graphics.drawable.GradientDrawable();
        selectedTab.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        selectedTab.setColor(0xF2FFFFFF);
        controls.setBackground(selectedTab);
        ((ImageView) ((ViewGroup) controls).getChildAt(0)).setImageTintList(ColorStateList.valueOf(0xFF16161A));
        LinearLayout.LayoutParams tabSize = new LinearLayout.LayoutParams(px(60), px(60));
        tabSize.setMarginStart(px(18));
        tabs.addView(controls, tabSize);
        View avatar = tile(R.drawable.ic_liquid_profile, context.getString(R.string.liquid_profile_title), true, true, profile);
        ((ImageView) ((ViewGroup) avatar).getChildAt(0)).setImageTintList(null);
        LinearLayout.LayoutParams avatarSize = new LinearLayout.LayoutParams(px(60), px(60));
        avatarSize.setMarginStart(px(18));
        tabs.addView(avatar, avatarSize);
        panel.addView(tabs);
        space(panel, 24);

        LinearLayout top = row();
        View saver = tile(R.drawable.ic_picture, context.getString(R.string.cc_screensaver), false, false, actions[0]);
        top.addView(saver, new LinearLayout.LayoutParams(px(210), px(210)));
        LinearLayout right = column();
        LinearLayout.LayoutParams rightSize = new LinearLayout.LayoutParams(px(210), px(210));
        rightSize.setMarginStart(px(20));
        top.addView(right, rightSize);
        right.addView(tile(R.drawable.ic_wifi, network, true, false, actions[1]), new LinearLayout.LayoutParams(-1, px(96)));
        space(right, 18);
        right.addView(tile(R.drawable.ic_input, context.getString(R.string.inputs), true, false, actions[2]), new LinearLayout.LayoutParams(-1, px(96)));
        panel.addView(top);
        space(panel, 18);
        LinearLayout middle = row();
        middle.addView(tile(R.drawable.ic_cc_bluetooth, context.getString(R.string.cc_bluetooth), true, false, actions[3]), new LinearLayout.LayoutParams(px(210), px(96)));
        LinearLayout.LayoutParams second = new LinearLayout.LayoutParams(px(210), px(96));
        second.setMarginStart(px(20));
        middle.addView(tile(R.drawable.ic_settings, context.getString(R.string.settings), true, false, actions[4]), second);
        panel.addView(middle);
        space(panel, 18);
        LinearLayout bottom = row();
        int[] icons = {R.drawable.ic_apps, R.drawable.ic_cc_accessibility, R.drawable.ic_home_settings_pill, R.drawable.ic_cc_search};
        int[] labels = {R.string.app_library, R.string.cc_accessibility, R.string.launcher_settings, R.string.cc_search};
        for (int i = 0; i < icons.length; i++) {
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(px(96), px(96));
            if (i > 0) size.setMarginStart(px(18));
            bottom.addView(tile(icons[i], context.getString(labels[i]), true, true, actions[i + 5]), size);
        }
        panel.addView(bottom);
        // Keep Up inside the tab strip and prevent lateral navigation escaping to Home.
        controls.setNextFocusUpId(controls.getId());
        saver.setNextFocusUpId(controls.getId());
        controls.setNextFocusDownId(saver.getId());
        avatar.setNextFocusDownId(right.getChildAt(0).getId());
        controls.requestFocus();
    }

    PopupWindow show(View root) {
        panel.setAlpha(motion ? 0 : 1);
        panel.setTranslationX(motion ? px(16) : 0);
        popup.showAtLocation(root, Gravity.TOP | Gravity.END, px(50), px(50));
        if (motion) panel.animate().alpha(1).translationX(0).setDuration(220).setInterpolator(LiquidStyle.EASE).start();
        return popup;
    }

    private View tile(int icon, String label, boolean pill, boolean iconOnly, Runnable action) {
        LinearLayout tile = pill && !iconOnly ? row() : column();
        tile.setId(View.generateViewId());
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(px(12), px(10), px(12), px(10));
        tile.setBackground(new LiquidSurface(density, pill));
        tile.setFocusable(true);
        tile.setClickable(true);
        tile.setContentDescription(label);
        ImageView glyph = new ImageView(context);
        glyph.setImageResource(icon);
        glyph.setDuplicateParentStateEnabled(true);
        glyph.setImageTintList(ink);
        tile.addView(glyph, new LinearLayout.LayoutParams(px(pill ? 40 : 82), px(pill ? 40 : 82)));
        if (!iconOnly) {
            TextView title = new TextView(context);
            title.setText(label);
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(23));
            title.setTextColor(ink);
            title.setDuplicateParentStateEnabled(true);
            title.setGravity(pill ? Gravity.CENTER_VERTICAL : Gravity.CENTER);
            title.setMaxLines(2);
            LinearLayout.LayoutParams labelSize = new LinearLayout.LayoutParams(pill ? 0 : -1, -2, pill ? 1 : 0);
            if (pill) labelSize.setMarginStart(px(12)); else labelSize.topMargin = px(14);
            tile.addView(title, labelSize);
        }
        tile.setOnFocusChangeListener((view, focused) -> view.animate()
                .scaleX(focused ? 1.05f : 1).scaleY(focused ? 1.05f : 1)
                .setDuration(motion ? 180 : 0).setInterpolator(LiquidStyle.EASE).start());
        tile.setOnClickListener(v -> { if (action != null) { popup.dismiss(); action.run(); } });
        return tile;
    }

    private int px(float value) { return Math.round(value * unit); }
    private LinearLayout column() { LinearLayout v = new LinearLayout(context); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private LinearLayout row() { LinearLayout v = new LinearLayout(context); v.setOrientation(LinearLayout.HORIZONTAL); return v; }
    private void space(LinearLayout parent, int height) { parent.addView(new View(context), new LinearLayout.LayoutParams(1, px(height))); }
}
