package id.laukpauk.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import id.laukpauk.core.CustomColors;

/**
 * Port of lib/global-components/Loading.dart: a centred green spinner around the Laukpauk icon with
 * an optional title / sub title.
 *
 *   Dart: Loading(title: 'Memuat...', subTitle: 'Mohon tunggu')
 *   Java: Loading.view(ctx, "Memuat...", "Mohon tunggu")
 */
public final class Loading {
    private Loading() {}

    public static View view(Context ctx) {
        return view(ctx, null, null, 50f, 50f);
    }

    public static View view(Context ctx, String title, String subTitle) {
        return view(ctx, title, subTitle, 50f, 50f);
    }

    /**
     * @param indicatorWidthDp  Dart circularProgressIndicatorSize.width (default 50)
     * @param indicatorHeightDp Dart circularProgressIndicatorSize.height (default 50)
     */
    public static View view(Context ctx, String title, String subTitle, float indicatorWidthDp,
                            float indicatorHeightDp) {
        FrameLayout center = new FrameLayout(ctx);

        LinearLayout column = Ui.column(ctx);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(column, 0, indicatorHeightDp, 0, indicatorHeightDp);

        FrameLayout indicator = new FrameLayout(ctx);
        ProgressBar bar = new ProgressBar(ctx);
        bar.setIndeterminate(true);
        bar.setIndeterminateTintList(ColorStateList.valueOf(CustomColors.green));
        indicator.addView(bar, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        ImageView logo = new ImageView(ctx);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Ui.setAsset(logo, "laukpauk-icon.png");
        FrameLayout.LayoutParams logoLp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH);
        int inset = Ui.dp(ctx, 7.5f);
        logoLp.setMargins(inset, inset, inset, inset);
        indicator.addView(logo, logoLp);

        column.addView(indicator, new LinearLayout.LayoutParams(
                Ui.dp(ctx, indicatorWidthDp), Ui.dp(ctx, indicatorHeightDp)));

        if (title != null) {
            TextView t = Ui.text(ctx, title, Txt.textMD(ctx).bold());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            lp.topMargin = Ui.dp(ctx, 25);
            column.addView(t, lp);
        }
        if (subTitle != null) {
            TextView s = Ui.text(ctx, subTitle, Txt.textXS(ctx).color(CustomColors.textMuted));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            lp.topMargin = Ui.dp(ctx, 10);
            column.addView(s, lp);
        }

        center.addView(column, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
        return center;
    }

    /**
     * The small plain spinner the Dart screens show while loading:
     * Center(child: SizedBox(width: 25, height: 25, child: CircularProgressIndicator(strokeWidth: 1.5))).
     */
    public static View spinner(Context ctx) {
        FrameLayout center = new FrameLayout(ctx);
        ProgressBar bar = new ProgressBar(ctx);
        bar.setIndeterminate(true);
        center.addView(bar, new FrameLayout.LayoutParams(Ui.dp(ctx, 25), Ui.dp(ctx, 25), Gravity.CENTER));
        return center;
    }
}
