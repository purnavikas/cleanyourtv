package com.gathin.tvhome;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Shared visual treatment for actions attached to a Home or library card. */
final class LiquidOptionStyle {
    private LiquidOptionStyle() { }
    static float unit(View view) { return view.getResources().getDisplayMetrics().widthPixels / 1920f; }
    static int width(View view) { return Math.round(520 * unit(view)); }
    static void panel(View panel) {
        float unit = unit(panel);
        int inset = Math.round(18 * unit);
        panel.setPadding(inset, inset, inset, inset);
        panel.setBackground(LiquidSurface.options(panel.getResources().getDisplayMetrics().density, 54 * unit));
    }
    static void row(View row, int iconResource) {
        float unit = unit(row);
        row.setLayoutParams(new LinearLayout.LayoutParams(-1, Math.round(84 * unit)));
        row.setPadding(Math.round(24 * unit), 0, Math.round(24 * unit), 0);
        ImageView icon = row.findViewById(R.id.icon);
        icon.setVisibility(View.VISIBLE);
        icon.setImageResource(iconResource);
        icon.setLayoutParams(new LinearLayout.LayoutParams(Math.round(32 * unit), Math.round(32 * unit)));
        TextView title = row.findViewById(R.id.title);
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28 * unit);
        title.setFontFeatureSettings("kern");
        LinearLayout.LayoutParams labelSize = (LinearLayout.LayoutParams) title.getLayoutParams();
        labelSize.setMarginStart(Math.round(20 * unit));
        labelSize.setMarginEnd(0);
        title.setLayoutParams(labelSize);
        GradientDrawable selected = new GradientDrawable();
        selected.setColor(0xFAFFFFFF);
        selected.setCornerRadius(42 * unit);
        StateListDrawable background = new StateListDrawable();
        background.addState(new int[]{android.R.attr.state_focused}, selected);
        background.addState(new int[]{android.R.attr.state_pressed}, selected);
        background.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        row.setBackground(background);
    }
}
