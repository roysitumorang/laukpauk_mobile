package id.laukpauk.view.pembeli;

import android.app.Activity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import id.laukpauk.ui.Ui;

/**
 * Dart: ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text), duration: ...)).
 * Helper of the detail screens of this package (DetailProduk, DetailPaket, DetailVoucher).
 */
final class DetailSnack {
    private DetailSnack() {}

    /** @param textSizeSp 0 keeps the default size */
    static Snackbar show(Activity a, String text, int seconds, float textSizeSp) {
        View anchor = a.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(anchor, text, seconds * 1000);
        style(snackbar.getView(), textSizeSp, a);
        snackbar.show();
        return snackbar;
    }

    private static void style(View v, float sizeSp, Activity a) {
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                style(g.getChildAt(i), sizeSp, a);
            }
        } else if (v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setTypeface(Ui.font(a));
            if (!(v instanceof Button) && sizeSp > 0) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
                tv.setMaxLines(4);
            }
        }
    }
}
