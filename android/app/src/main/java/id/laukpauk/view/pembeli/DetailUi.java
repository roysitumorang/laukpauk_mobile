package id.laukpauk.view.pembeli;

import android.content.Context;
import android.graphics.Outline;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import id.laukpauk.R;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Ui;

/**
 * Small view helpers shared by the detail screens of this package (DetailProduk, DetailPaket,
 * Voucher, DetailVoucher and their components): Material AppBar, SnackBar, RefreshIndicator,
 * the 15dp spinner and the default (unstyled) Flutter Text.
 */
public final class DetailUi {
    private DetailUi() {}

    private static final String SNACKBAR_TAG = "detailui.snackbar";

    /** Flutter's default Text style (Material bodyText2): 14, black87. */
    public static TextView defaultText(Context ctx, CharSequence s) {
        return Ui.text(ctx, s, 14f, 0xDD000000);
    }

    /**
     * Dart: AppBar(title: Text(title), backgroundColor: CustomColors.green). The app runs with
     * ThemeData(platform: iOS): back chevron, centred white title, elevation 4. Includes the
     * status bar inset (the screen draws edge to edge).
     */
    public static View appBar(final BaseActivity a, String title) {
        LinearLayout bar = Ui.column(a);
        bar.setBackgroundColor(CustomColors.green);
        bar.setElevation(Ui.dp(a, 4));
        Layouts.tintStatusBar(a, CustomColors.green); // edge-to-edge: BaseActivity insets the content

        FrameLayout toolbar = new FrameLayout(a);
        bar.addView(toolbar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(a, 56)));

        TextView titleView = Ui.text(a, title, 20f, CustomColors.white);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        titleView.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP,
                Gravity.CENTER_VERTICAL);
        titleLp.leftMargin = Ui.dp(a, 56);
        titleLp.rightMargin = Ui.dp(a, 56);
        toolbar.addView(titleView, titleLp);

        FrameLayout back = new FrameLayout(a);
        ImageView icon = Ui.icon(a, R.drawable.ic_arrow_back_ios, 24, CustomColors.white);
        back.addView(icon, new FrameLayout.LayoutParams(Ui.dp(a, 24), Ui.dp(a, 24), Gravity.CENTER));
        Layouts.ripple(back, 0x33FFFFFF, 28f);
        back.setOnClickListener(v -> a.pop());
        toolbar.addView(back, new FrameLayout.LayoutParams(Ui.dp(a, 56), Ui.dp(a, 56),
                Gravity.LEFT | Gravity.CENTER_VERTICAL));
        return bar;
    }

    /**
     * Dart: Center(child: SizedBox(width: size, height: size, child:
     * CircularProgressIndicator(strokeWidth: 1.5))).
     */
    public static View smallSpinner(Context ctx, float sizeDp) {
        FrameLayout center = new FrameLayout(ctx);
        ProgressBar bar = new ProgressBar(ctx);
        bar.setIndeterminate(true);
        center.addView(bar, new FrameLayout.LayoutParams(Ui.dp(ctx, sizeDp), Ui.dp(ctx, sizeDp),
                Gravity.CENTER));
        return center;
    }

    /**
     * Dart: RefreshIndicator(onRefresh: ..., child: ListView(...)). The returned layout fills its
     * parent; `content` is put into a ScrollView whose content stretches to the viewport (so a
     * "minHeight: constraints.maxHeight" container can centre its children). Call
     * {@code setRefreshing(false)} on it once the reload is done.
     */
    public static SwipeRefreshLayout refreshable(Context ctx, View content, final Runnable onRefresh,
                                                 final boolean fillViewport) {
        SwipeRefreshLayout refresh = new SwipeRefreshLayout(ctx);
        refresh.setColorSchemeColors(CustomColors.green);
        android.widget.ScrollView scroll = new android.widget.ScrollView(ctx);
        scroll.setFillViewport(fillViewport);
        scroll.addView(content, new ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        refresh.addView(scroll, new ViewGroup.LayoutParams(Ui.MATCH, Ui.MATCH));
        refresh.setOnRefreshListener(() -> onRefresh.run());
        return refresh;
    }

    /**
     * Dart: ScaffoldMessenger.of(context).showSnackBar(SnackBar(...)). The bar is shown at the
     * bottom of `host` (the screen's body area, so it sits above a Footer like Flutter's fixed
     * SnackBar does) and removed after `durationMs`.
     *
     * @param actionLabel null = no SnackBarAction
     */
    public static void snackbar(final FrameLayout host, String text, float textSizeDp,
                                long durationMs, String actionLabel, int actionColor,
                                final Runnable action) {
        Context ctx = host.getContext();
        View old = host.findViewWithTag(SNACKBAR_TAG);
        if (old != null) {
            host.removeView(old);
        }

        final LinearLayout bar = new LinearLayout(ctx);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setTag(SNACKBAR_TAG);
        bar.setBackgroundColor(0xFF323232);
        bar.setElevation(Ui.dp(ctx, 6));
        bar.setClickable(true);
        Ui.pad(bar, 24, 12, actionLabel != null ? 12 : 24, 12);

        TextView message = Ui.text(ctx, text, textSizeDp, CustomColors.white);
        bar.addView(message, Ui.weightW(1f));

        if (actionLabel != null) {
            TextView actionView = Ui.text(ctx, actionLabel, textSizeDp, actionColor, true);
            Ui.padXY(actionView, 12, 8);
            Layouts.ripple(actionView, 0x33FFFFFF, 4f);
            actionView.setOnClickListener(v -> {
                if (bar.getParent() == host) {
                    host.removeView(bar);
                }
                if (action != null) {
                    action.run();
                }
            });
            bar.addView(actionView, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }

        host.addView(bar, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.BOTTOM));
        host.postDelayed(() -> {
            if (bar.getParent() == host) {
                host.removeView(bar);
            }
        }, durationMs);
    }

    /** A rounded-top outline for images that sit at the top of a rounded Card. */
    public static void clipTopRounded(View v, float radiusDp) {
        final float r = Ui.dp(v.getContext(), radiusDp);
        v.setClipToOutline(true);
        v.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), Math.round(view.getHeight() + r), r);
            }
        });
    }
}
