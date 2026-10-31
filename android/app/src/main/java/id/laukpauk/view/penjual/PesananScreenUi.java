package id.laukpauk.view.penjual;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.R;
import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Ui;

/**
 * Small Flutter-widget equivalents shared by the seller order screens
 * (HistoriPenjualan, PesananMasuk, ProsesPesanan, PesananItemTidakTersedia and their items):
 * Scaffold(appBar: AppBar(...)), Card(elevation: 5, ...), a scrollable TabBar and a context-safe
 * background-work helper.
 */
public final class PesananScreenUi {
    private PesananScreenUi() {}

    /** ThemeData default scaffoldBackgroundColor (Colors.grey[50]) used by screens without backgroundColor. */
    public static final int scaffoldBackground = 0xFFFAFAFA;

    /**
     * AppBar(title: Text(title), backgroundColor: CustomColors.green): status-bar space, a 56dp bar with the
     * (iOS-platform) back arrow and a centred white 20sp title, elevation 4.
     */
    public static View appBar(final Activity a, String title) {
        LinearLayout bar = Ui.column(a);
        bar.setBackgroundColor(CustomColors.green);
        bar.setElevation(Ui.dp(a, 4));
        Layouts.tintStatusBar(a, CustomColors.green); // edge-to-edge: BaseActivity insets the content

        FrameLayout toolbar = new FrameLayout(a);

        TextView titleView = Ui.text(a, title, 20f, CustomColors.white);
        titleView.setGravity(Gravity.CENTER);
        titleView.setSingleLine(true);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        FrameLayout.LayoutParams titleLp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.CENTER);
        titleLp.leftMargin = Ui.dp(a, 56);
        titleLp.rightMargin = Ui.dp(a, 56);
        toolbar.addView(titleView, titleLp);

        FrameLayout back = new FrameLayout(a);
        ImageView arrow = Ui.icon(a, R.drawable.ic_arrow_back_ios, 24, CustomColors.white);
        FrameLayout.LayoutParams arrowLp = new FrameLayout.LayoutParams(Ui.dp(a, 24), Ui.dp(a, 24), Gravity.CENTER);
        back.addView(arrow, arrowLp);
        Ui.onTap(back, new Runnable() {
            @Override
            public void run() {
                if (a instanceof id.laukpauk.ui.BaseActivity) {
                    ((id.laukpauk.ui.BaseActivity) a).goBack(); // Android 16: onBackPressed() is not dispatched
                } else {
                    a.finish();
                }
            }
        });
        toolbar.addView(back, new FrameLayout.LayoutParams(Ui.dp(a, 56), Ui.dp(a, 56), Gravity.START | Gravity.CENTER_VERTICAL));

        bar.addView(toolbar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(a, 56)));
        return bar;
    }

    /** LinearLayout params with margins in dp. */
    public static LinearLayout.LayoutParams lp(Context ctx, int width, int height, float l, float t, float r, float b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.setMargins(Ui.dp(ctx, l), Ui.dp(ctx, t), Ui.dp(ctx, r), Ui.dp(ctx, b));
        return p;
    }

    /**
     * Card(elevation: 5, shape: RoundedRectangleBorder(borderRadius: circular(10))): white, elevated,
     * rounded; the default Card margin of 4 is applied through the returned layout params when added to
     * a vertical LinearLayout (the view's own layout params carry the margin).
     */
    public static LinearLayout card(Context ctx) {
        LinearLayout card = Layouts.card(ctx, CustomColors.white, 10f, 5f);
        card.setLayoutParams(lp(ctx, Ui.MATCH, Ui.WRAP, 4, 4, 4, 4));
        return card;
    }

    /**
     * TabBar(isScrollable: true, labelColor/indicatorColor green, unselectedLabelColor textMuted) with text tabs.
     * Tabs are 16dp padded, 46dp high, with a 2dp indicator under the selected one. The strip is centred when it
     * fits and scrolls horizontally otherwise.
     */
    public static HorizontalScrollView tabBar(final Activity a, String[] labels, int selected, float fontSizeSp,
                                              final Async.Done<Integer> onTap) {
        HorizontalScrollView scroll = new HorizontalScrollView(a);
        scroll.setHorizontalScrollBarEnabled(false);

        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            boolean isSelected = i == selected;

            FrameLayout tab = new FrameLayout(a);
            TextView label = Ui.text(a, labels[i], fontSizeSp,
                    isSelected ? CustomColors.green : CustomColors.textMuted);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            Ui.pad(label, 16, 0, 16, 0);
            FrameLayout.LayoutParams labelLp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.dp(a, 46), Gravity.TOP);
            tab.addView(label, labelLp);

            if (isSelected) {
                View indicator = new View(a);
                indicator.setBackgroundColor(CustomColors.green);
                tab.addView(indicator, new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(a, 2), Gravity.BOTTOM));
            }

            Ui.onTap(tab, new Runnable() {
                @Override
                public void run() {
                    if (onTap != null) {
                        onTap.run(index);
                    }
                }
            });
            row.addView(tab, new LinearLayout.LayoutParams(Ui.WRAP, Ui.dp(a, 48)));
        }

        scroll.addView(row, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        scrollLp.gravity = Gravity.CENTER_HORIZONTAL;
        scroll.setLayoutParams(scrollLp);
        return scroll;
    }

    /**
     * Runs blocking work off the main thread and delivers the result on the main thread. Uses the hosting
     * BaseActivity (skipping the callback once the screen is gone) when the context is one.
     */
    public static <T> void run(Context ctx, Async.Work<T> work, Async.Done<T> done) {
        if (ctx instanceof BaseActivity) {
            ((BaseActivity) ctx).async(work, done);
        } else {
            Async.run(work, done);
        }
    }
}
