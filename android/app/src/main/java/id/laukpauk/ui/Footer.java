package id.laukpauk.ui;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import id.laukpauk.core.CustomColors;
import id.laukpauk.view.pembeli.HomePageActivity;
import id.laukpauk.view.pembeli.PesananSayaActivity;
import id.laukpauk.view.pembeli.PromoActivity;
import id.laukpauk.view.pembeli.ResepActivity;
import id.laukpauk.view.pembeli.UserAndDashboardActivity;

/**
 * lib/global-components/Footer.dart: the bottom navigation bar. Every item replaces the whole
 * back stack (Navigator.pushAndRemoveUntil(..., (route) => false)). Dart's currentIndex stays 0 and
 * the selected and unselected colour are the same (grey[400]).
 */
public class Footer extends LinearLayout {
    private static final String[][] MENUS = {
            {"beranda.png", "Beranda"},
            {"pesanan.png", "Pesanan"},
            {"pahlawan-dapur.png", "Resep"},
            {"promo.png", "Promo"},
            {"akun.png", "Akun"}
    };

    private static final Class<?>[] DESTINATIONS = {
            HomePageActivity.class,
            PesananSayaActivity.class,
            ResepActivity.class,
            PromoActivity.class,
            UserAndDashboardActivity.class
    };

    public Footer(Context ctx) {
        super(ctx);
        float hb = Ui.hb(ctx);
        setOrientation(HORIZONTAL);
        setBackgroundColor(CustomColors.nearlyWhite);
        setElevation(Ui.dp(ctx, 10));

        for (int i = 0; i < MENUS.length; i++) {
            final int index = i;
            LinearLayout item = Ui.column(ctx);
            item.setGravity(Gravity.CENTER);
            Ui.pad(item, 0, 6, 0, 6);

            ImageView icon = Ui.asset(ctx, "icons/bottomNavigation/" + MENUS[i][0], hb * 5.5f, hb * 5.5f);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 5.5f), Ui.dp(ctx, hb * 5.5f));
            ilp.bottomMargin = Ui.dp(ctx, 2.5f);
            icon.setLayoutParams(ilp);
            item.addView(icon);

            TextView label = Ui.text(ctx, MENUS[i][1], hb * 3f, CustomColors.grey400);
            label.setGravity(Gravity.CENTER);
            item.addView(label, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

            Layouts.ripple(item, 0x22000000, 0f);
            item.setOnClickListener(v -> changeIndex(index));
            addView(item, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        }
    }

    @SuppressWarnings("unchecked")
    private void changeIndex(int index) {
        Context ctx = getContext();
        Class<? extends Activity> target = (Class<? extends Activity>) DESTINATIONS[index];
        Nav.pushAndRemoveUntil(ctx, target, null);
    }
}
