package id.laukpauk.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import id.laukpauk.core.CustomColors;

/**
 * Port of lib/global-components/CustomButton.dart.
 *
 *   Dart: CustomButton.button(context, 'OK', () {...}, color: CustomColors.green, isBlock: true)
 *   Java: CustomButton.button(ctx, "OK", () -> {...}, new CustomButton.Opts().color(CustomColors.green).block())
 *
 * The result is a {@link Btn}: call setBusy(true/false) to toggle `isBusy`, setText(..) to relabel.
 */
public final class CustomButton {
    private CustomButton() {}

    public static final int themeColor = 0xFF66CC99;
    public static final int defaultColor = 0xFFE9ECEF;
    public static final int primaryColor = 0xFF0069D9;
    public static final int secondaryColor = 0xFF5A6268;
    public static final int successColor = 0xFF218838;
    public static final int infoColor = 0xFF138496;
    public static final int dangerColor = 0xFFC82333;
    public static final int warningColor = 0xFFE0A800;

    public static final String lgSize = "lg";
    public static final String mdSize = "md";
    public static final String smSize = "sm";
    public static final String xsSize = "xs";

    /** The named parameters of CustomButton.button(...). All optional. */
    public static final class Opts {
        Integer color;
        Integer borderColor;
        float elevation = 0f;
        boolean isBlock;
        boolean isBusy;
        boolean outline;
        boolean isPill;
        String size;

        public Opts color(int c) {
            this.color = c;
            return this;
        }

        public Opts borderColor(int c) {
            this.borderColor = c;
            return this;
        }

        public Opts elevation(float e) {
            this.elevation = e;
            return this;
        }

        /** isBlock: true (fills the width). */
        public Opts block() {
            this.isBlock = true;
            return this;
        }

        public Opts block(boolean b) {
            this.isBlock = b;
            return this;
        }

        public Opts busy(boolean b) {
            this.isBusy = b;
            return this;
        }

        public Opts outline() {
            this.outline = true;
            return this;
        }

        public Opts outline(boolean b) {
            this.outline = b;
            return this;
        }

        public Opts pill() {
            this.isPill = true;
            return this;
        }

        public Opts pill(boolean b) {
            this.isPill = b;
            return this;
        }

        /** "lg" | "md" | "sm" | "xs". */
        public Opts size(String s) {
            this.size = s;
            return this;
        }
    }

    /** The button view. */
    public static final class Btn extends FrameLayout {
        private final TextView label;
        private final ProgressBar spinner;
        private final Runnable onTap;
        private boolean busy;

        Btn(Context ctx, TextView label, ProgressBar spinner, Runnable onTap) {
            super(ctx);
            this.label = label;
            this.spinner = spinner;
            this.onTap = onTap;
        }

        public boolean isBusy() {
            return busy;
        }

        /** Dart: isBusy. While busy the tap is ignored and a spinner replaces the label. */
        public void setBusy(boolean isBusy) {
            busy = isBusy;
            label.setVisibility(isBusy ? View.INVISIBLE : View.VISIBLE);
            spinner.setVisibility(isBusy ? View.VISIBLE : View.GONE);
        }

        public void setText(CharSequence text) {
            label.setText(text);
        }

        boolean tapAllowed() {
            return !busy;
        }

        void tap() {
            if (onTap != null) {
                onTap.run();
            }
        }
    }

    public static Btn button(Context ctx, String text, Runnable onTap) {
        return button(ctx, text, onTap, new Opts());
    }

    public static Btn button(Context ctx, String text, final Runnable onTap, Opts o) {
        if (o == null) {
            o = new Opts();
        }
        final float hb = Ui.hb(ctx);
        final int color = o.color != null ? o.color : defaultColor;
        final int borderColor = o.borderColor != null ? o.borderColor : 0x55E9ECEF;
        final String size = o.size != null ? o.size : mdSize;

        float pillCorner = hb * 7.5f;
        float padV = hb * 2.75f;
        float padH = hb * 7.5f;
        float fontSize = hb * 3.5f;

        if (lgSize.equals(size)) {
            padH = padH + 2.5f;
            padV = padV + 1.25f;
            fontSize = fontSize + 5f;
        }
        if (smSize.equals(size)) {
            padH = padH - 5f;
            padV = padV - 1.5f;
            fontSize = fontSize - 2.5f;
        }
        if (xsSize.equals(size)) {
            padH = padH - 7.5f;
            padV = padV - 2.75f;
            fontSize = fontSize - 3.5f;
        }

        final float radius = o.isPill ? pillCorner : 7.5f;

        TextView label = Ui.text(ctx, text, fontSize, o.outline ? color : 0xFFFFFFFF);
        label.setGravity(Gravity.CENTER);

        ProgressBar spinner = new ProgressBar(ctx);
        spinner.setIndeterminate(true);
        spinner.setVisibility(View.GONE);
        spinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(
                o.outline ? color : CustomColors.nearlyWhite));

        final Btn btn = new Btn(ctx, label, spinner, onTap);
        btn.setBackground(Ui.shape(ctx, o.outline ? CustomColors.nearlyWhite : color, radius,
                o.outline ? color : borderColor, 1f));
        Ui.pad(btn, padH, padV, padH, padV);
        if (o.elevation > 0f) {
            btn.setElevation(Ui.dp(ctx, o.elevation));
        }

        btn.addView(label, new FrameLayout.LayoutParams(o.isBlock ? Ui.MATCH : Ui.WRAP, Ui.WRAP, Gravity.CENTER));
        btn.addView(spinner, new FrameLayout.LayoutParams(Ui.dp(ctx, 15), Ui.dp(ctx, 15), Gravity.CENTER));

        Layouts.ripple(btn, o.outline ? (color & 0x00FFFFFF) | 0x33000000 : 0x55FFFFFF, radius);
        btn.setOnClickListener(v -> {
            if (btn.tapAllowed()) {
                btn.tap();
            }
        });
        btn.setLayoutParams(new LinearLayout.LayoutParams(o.isBlock ? Ui.MATCH : Ui.WRAP, Ui.WRAP));
        btn.setBusy(o.isBusy);
        return btn;
    }
}
