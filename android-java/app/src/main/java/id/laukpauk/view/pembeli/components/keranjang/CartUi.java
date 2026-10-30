package id.laukpauk.view.pembeli.components.keranjang;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/**
 * Small view helpers shared by the cart screens (Keranjang, EditKeranjangItem, MyGift ...):
 * Flutter's Material AppBar, TextButton and Card as used by those Dart files.
 */
public final class CartUi {
    private CartUi() {}

    /** Flutter's default primary / accent colour (Colors.blue): TextButton, RefreshIndicator, progress indicators. */
    public static final int MATERIAL_BLUE = 0xFF2196F3;
    /** Colors.grey[300]. */
    public static final int GREY_300 = 0xFFE0E0E0;
    /** Colors.grey (500). */
    public static final int GREY_500 = 0xFF9E9E9E;
    /** The default text colour of a Material TextField (black87). */
    public static final int BLACK87 = 0xDD000000;

    /** The Activity behind a (possibly wrapped) context, or null. */
    public static Activity activity(Context c) {
        Context current = c;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            current = ((ContextWrapper) current).getBaseContext();
        }
        return null;
    }

    /** The BaseActivity behind a (possibly wrapped) context, or null. */
    public static BaseActivity base(Context c) {
        Activity a = activity(c);
        return (a instanceof BaseActivity) ? (BaseActivity) a : null;
    }

    /**
     * Scaffold's AppBar(backgroundColor: CustomColors.green, title: Text(title)): green bar that
     * also paints the status-bar inset, white centred title (the Dart app runs with
     * TargetPlatform.iOS, which centres AppBar titles), optional white back arrow.
     */
    public static View appBar(final Activity a, String title, boolean withBack) {
        LinearLayout box = Ui.column(a);
        box.setBackgroundColor(CustomColors.green);
        box.setElevation(Ui.dp(a, 4));
        Layouts.tintStatusBar(a, CustomColors.green); // edge-to-edge: BaseActivity insets the content

        FrameLayout bar = new FrameLayout(a);

        TextView t = Ui.text(a, title, 20f, CustomColors.white);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH);
        titleLp.leftMargin = Ui.dp(a, 56);
        titleLp.rightMargin = Ui.dp(a, 56);
        bar.addView(t, titleLp);

        if (withBack) {
            FrameLayout button = new FrameLayout(a);
            ImageView icon = Ui.icon(a, R.drawable.ic_arrow_back, 24, CustomColors.white);
            button.addView(icon, new FrameLayout.LayoutParams(Ui.dp(a, 24), Ui.dp(a, 24), Gravity.CENTER));
            Layouts.ripple(button, 0x33FFFFFF, 24f);
            button.setOnClickListener(v -> a.finish());
            FrameLayout.LayoutParams buttonLp = new FrameLayout.LayoutParams(Ui.dp(a, 48), Ui.dp(a, 48),
                    Gravity.START | Gravity.CENTER_VERTICAL);
            buttonLp.leftMargin = Ui.dp(a, 4);
            bar.addView(button, buttonLp);
        }

        box.addView(bar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(a, 56)));
        return box;
    }

    /**
     * Flutter's TextButton as used in the alert actions: blue label (not upper-cased), 16/8 padding,
     * min size 64x36.
     */
    public static TextView textButton(Context ctx, String label, final Runnable onTap) {
        TextView tv = Ui.text(ctx, label, new Txt.Style(14f).color(MATERIAL_BLUE).letterSpacing(0.09f));
        Ui.padXY(tv, 16, 8);
        tv.setGravity(Gravity.CENTER);
        tv.setMinWidth(Ui.dp(ctx, 64));
        tv.setMinHeight(Ui.dp(ctx, 36));
        tv.setBackground(Ui.shape(ctx, CustomColors.transparent, 4f));
        Layouts.ripple(tv, 0x222196F3, 4f);
        tv.setOnClickListener(v -> onTap.run());
        return tv;
    }

    /** A circular progress indicator of the given size with stroke 1.5 (the default blue tint). */
    public static ProgressBar smallSpinner(Context ctx, float sizeDp) {
        ProgressBar bar = new ProgressBar(ctx);
        bar.setIndeterminate(true);
        bar.setIndeterminateTintList(ColorStateList.valueOf(MATERIAL_BLUE));
        bar.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(ctx, sizeDp), Ui.dp(ctx, sizeDp)));
        return bar;
    }

    /**
     * A Material Card: white, rounded, elevated, default margin of 4 on every side. The returned
     * LinearLayout is the card itself; its layout params already carry the margin and are
     * MATCH_PARENT x WRAP_CONTENT inside a LinearLayout (FrameLayout params when `inFrame`).
     */
    public static LinearLayout card(Context ctx, int fill, float radiusDp, float elevationDp,
                                    boolean inFrame) {
        LinearLayout card = Layouts.card(ctx, fill, radiusDp, elevationDp);
        int m = Ui.dp(ctx, 4);
        if (inFrame) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            lp.setMargins(m, m, m, m);
            card.setLayoutParams(lp);
        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            lp.setMargins(m, m, m, m);
            card.setLayoutParams(lp);
        }
        return card;
    }
}
