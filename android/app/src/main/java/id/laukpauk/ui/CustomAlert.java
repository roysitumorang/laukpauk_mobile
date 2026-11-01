package id.laukpauk.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;

/**
 * Port of lib/global-components/CustomAlert.dart.
 *
 *   Dart:  final ok = await CustomAlert.konfirmasi(context, title: 'Hapus', subTitle: 'Yakin?');
 *   Java:  CustomAlert.konfirmasi(this, "Hapus", "Yakin?", null, ok -> { if (Boolean.TRUE.equals(ok)) ... });
 *
 *   Dart:  CustomAlert.textComponent<bool>(context, title: t, subTitle: s, actions: Row(...Navigator.pop(context, true)...));
 *   Java:  CustomAlert.<Boolean>textComponent(this, null, t, s, handle -> row(... handle.close(true) ...), null, false, result -> {...});
 */
public final class CustomAlert {
    private CustomAlert() {}

    private static final float RADIUS = 15f;

    /** The standard "Laukpauk" logo icon the Dart code used when no icon was given. */
    private static View defaultIcon(Activity a) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(a, 50), Ui.WRAP);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        android.widget.ImageView iv = Ui.asset(a, "laukpauk-icon.png", 50);
        iv.setLayoutParams(lp);
        return iv;
    }

    /**
     * Dart: CustomAlert.textComponent<T>(...).
     *
     * @param icon         null = the default Laukpauk logo (Dart `icon`)
     * @param actions      builds the buttons row; use handle.close(value) for Navigator.pop(context, value). May be null.
     * @param moreWidgets  extra view under the sub title, or null
     * @param isDismissible Dart `isDismissible` (barrierDismissible)
     * @param onClosed     receives the value passed to handle.close(..), or null; may be null itself
     */
    public static <T> void textComponent(final Activity a, View icon, String title, String subTitle,
                                         final Modal.Content<T> actions, final View moreWidgets,
                                         boolean isDismissible, Async.Done<T> onClosed) {
        final View iconView = icon != null ? icon : defaultIcon(a);
        final String t = title;
        final String s = subTitle;
        Modal.<T>dialog(a, isDismissible, false, handle -> {
            float hb = Ui.hb(a);
            LinearLayout box = Ui.column(a);
            box.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.pad(box, 16, 20, 16, 20);
            box.setBackground(Ui.shape(a, CustomColors.nearlyWhite, RADIUS));

            if (iconView.getParent() instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) iconView.getParent()).removeView(iconView);
            }
            LinearLayout.LayoutParams iconLp = iconView.getLayoutParams() instanceof LinearLayout.LayoutParams
                    ? (LinearLayout.LayoutParams) iconView.getLayoutParams()
                    : new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            iconLp.gravity = Gravity.CENTER_HORIZONTAL;
            box.addView(iconView, iconLp);
            box.addView(Ui.spaceH(a, 15));

            TextView titleView = Ui.text(a, t, new Txt.Style(hb * 4.5f).bold());
            titleView.setGravity(Gravity.CENTER);
            box.addView(titleView, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            box.addView(Ui.spaceH(a, 15));

            TextView sub = Ui.text(a, s, new Txt.Style(hb * 3.25f));
            sub.setGravity(Gravity.CENTER);
            box.addView(sub, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            if (moreWidgets != null) {
                if (moreWidgets.getParent() instanceof android.view.ViewGroup) {
                    ((android.view.ViewGroup) moreWidgets.getParent()).removeView(moreWidgets);
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
                lp.topMargin = Ui.dp(a, 10);
                box.addView(moreWidgets, lp);
            }
            if (actions != null) {
                View actionsView = actions.build(handle);
                if (actionsView != null) {
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
                    lp.topMargin = Ui.dp(a, 16);
                    box.addView(actionsView, lp);
                }
            }
            return box;
        }, onClosed);
    }

    /** A centred row of views (Dart: Row(mainAxisAlignment: MainAxisAlignment.center, children: [...])). */
    public static LinearLayout actionsRow(Activity a, View... children) {
        LinearLayout row = Ui.row(a);
        row.setGravity(Gravity.CENTER);
        for (View c : children) {
            if (c.getParent() instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) c.getParent()).removeView(c);
            }
            row.addView(c);
        }
        return row;
    }

    /** Flutter's TextButton (plain coloured label, ripple). */
    public static TextView textButton(Activity a, String label, final Runnable onTap) {
        TextView tv = Ui.text(a, label.toUpperCase(), Ui.hb(a) * 3.5f, 0xFF0069D9, true);
        Ui.padXY(tv, 16, 10);
        tv.setGravity(Gravity.CENTER);
        Layouts.ripple(tv, 0x220069D9, 4f);
        tv.setOnClickListener(v -> onTap.run());
        return tv;
    }

    /** Dart: CustomAlert.urlCantBeLaunched(context, url: url). */
    public static void urlCantBeLaunched(final Activity a, String url) {
        String title = "URL Tidak Dapat Diakses";
        String subTitle = "URL " + (url == null ? "" : url)
                + " tidak dapat dijalankan, kemungkinan karena alamat url tidak ditemukan atau format url yang tidak valid!";
        Modal.Content<Object> actions = handle -> actionsRow(a,
                CustomButton.button(a, "OK", handle::close,
                        new CustomButton.Opts().color(CustomColors.textDanger)));
        CustomAlert.<Object>textComponent(a, null, title, subTitle, actions, null, false, null);
    }

    /** Dart: CustomAlert.konfirmasi -> Future<bool?>. `done` gets true / false / null (dismissed). */
    public static void konfirmasi(final Activity a, String title, String subTitle, View moreWidget,
                                  Async.Done<Boolean> done) {
        Modal.Content<Boolean> actions = handle -> actionsRow(a,
                textButton(a, "Tidak", () -> handle.close(Boolean.FALSE)),
                textButton(a, "Ya", () -> handle.close(Boolean.TRUE)));
        CustomAlert.<Boolean>textComponent(a, null, title, subTitle, actions, moreWidget, false, done);
    }

    /** Convenience for the very common "title + message + one OK button" alert. */
    public static void info(final Activity a, String title, String subTitle, Runnable afterOk) {
        Modal.Content<Object> actions = handle -> actionsRow(a,
                CustomButton.button(a, "OK", handle::close,
                        new CustomButton.Opts().color(CustomColors.green)));
        CustomAlert.<Object>textComponent(a, null, title, subTitle, actions, null, false,
                r -> {
                    if (afterOk != null) {
                        afterOk.run();
                    }
                });
    }
}
