package id.laukpauk.view.pembeli.components.detailtransaksi;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.core.CustomColors;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** Small shared UI bits of the buyer transaction screens (Dart: titleSize/contentSize/catatanSize + Card). */
public final class TransaksiUi {
    private TransaksiUi() {}

    /** Flutter's default text colour (black87). */
    public static final int BLACK87 = 0xDD000000;

    /** TextStyle(fontSize: horizontalBlock * 3.75, fontWeight: w700, color: nearlyBlack). */
    public static TextView title(Context ctx, CharSequence s) {
        return title(ctx, s, CustomColors.nearlyBlack);
    }

    public static TextView title(Context ctx, CharSequence s, int color) {
        return Ui.text(ctx, s, new Txt.Style(Ui.hb(ctx) * 3.75f).color(color).bold());
    }

    /** TextStyle(fontSize: horizontalBlock * 3.25, color: nearlyBlack). */
    public static TextView content(Context ctx, CharSequence s) {
        return content(ctx, s, CustomColors.nearlyBlack, false, false, false);
    }

    public static TextView content(Context ctx, CharSequence s, int color) {
        return content(ctx, s, color, false, false, false);
    }

    public static TextView content(Context ctx, CharSequence s, int color, boolean bold, boolean italic,
                                   boolean strike) {
        Txt.Style st = new Txt.Style(Ui.hb(ctx) * 3.25f).color(color).bold(bold);
        if (italic) {
            st.italic();
        }
        if (strike) {
            st.strike();
        }
        return Ui.text(ctx, s, st);
    }

    /** TextStyle(fontSize: horizontalBlock * 2.5, color: nearlyBlack). */
    public static TextView catatan(Context ctx, CharSequence s, int color) {
        return Ui.text(ctx, s, new Txt.Style(Ui.hb(ctx) * 2.5f).color(color));
    }

    /** Text with an explicit size factor (x horizontalBlock), colour and weight. */
    public static TextView hbText(Context ctx, CharSequence s, float factor, int color, boolean bold) {
        return Ui.text(ctx, s, new Txt.Style(Ui.hb(ctx) * factor).color(color).bold(bold));
    }

    /**
     * Card(elevation: 5, shape: RoundedRectangleBorder(radius 10)) with the Card's default 4dp margin, whose
     * child is padded by (padH, padV). Returns the inner column.
     */
    public static LinearLayout card(Context ctx, float padH, float padV) {
        LinearLayout c = Ui.column(ctx);
        c.setBackground(Ui.shape(ctx, CustomColors.white, 10f));
        c.setElevation(Ui.dp(ctx, 5));
        Ui.pad(c, padH, padV, padH, padV);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        int m = Ui.dp(ctx, 4);
        lp.setMargins(m, m, m, m);
        c.setLayoutParams(lp);
        return c;
    }

    /** A horizontal row that lays out [left ... right] (Row(mainAxisAlignment: spaceBetween)). */
    public static LinearLayout spaceBetween(Context ctx, View left, View right) {
        LinearLayout row = Ui.row(ctx);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(left, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        row.addView(right, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        return row;
    }

    /** Opens `url` with an ACTION_VIEW intent; false when nothing can handle it (canLaunchUrl == false). */
    public static boolean launch(Activity a, String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            a.startActivity(i);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            return false;
        }
    }
}
