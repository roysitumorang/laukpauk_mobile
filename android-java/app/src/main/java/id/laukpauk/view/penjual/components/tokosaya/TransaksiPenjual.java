package id.laukpauk.view.penjual.components.tokosaya;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import id.laukpauk.core.Json;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.UserAndDashboardActivity;
import id.laukpauk.view.penjual.HistoriPenjualanActivity;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.PesananMasukActivity;

/** Port of lib/views/penjual/components/TokoSaya/TransaksiPenjual.dart. */
public class TransaksiPenjual extends LinearLayout {
    public TransaksiPenjual(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        LinearLayout card = PenjualUi.cardPadded(ctx, 15, 10);
        card.setLayoutParams(new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(card);

        card.addView(PenjualUi.text(ctx, "Transaksi Penjual", 4f, 0xDD000000, true));
        card.addView(Ui.spaceH(ctx, 15));

        final Context c = ctx;
        card.addView(PenjualUi.spaceAround(ctx,
                item(ctx, "icons/orderan-baru.png", "Orderan Baru Masuk",
                        () -> Nav.push(c, PesananMasukActivity.class)),
                item(ctx, "icons/delivery.png", "Dalam Perjalanan",
                        () -> Nav.push(c, PesananMasukActivity.class, Json.of("statusTransaksi", 1))),
                item(ctx, "icons/completed.png", "Pesanan Diterima",
                        () -> Nav.push(c, PesananMasukActivity.class, Json.of("statusTransaksi", 2))),
                item(ctx, "icons/penjualan.png", "Histori Penjualan",
                        () -> Nav.push(c, HistoriPenjualanActivity.class)),
                item(ctx, "user-icon.png", "Akun Saya",
                        () -> Nav.pushAndRemoveUntil(c, UserAndDashboardActivity.class))));
        card.addView(Ui.spaceH(ctx, 10));
    }

    /** Container(width: hb*17.5) { Column { Image(hb*9), SizedBox(10), Text(centered, hb*3) } } in an InkWell. */
    private static View item(Context ctx, String asset, String label, Runnable onTap) {
        float hb = Ui.hb(ctx);
        LinearLayout col = Ui.column(ctx);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.addView(Ui.asset(ctx, asset, hb * 9, hb * 9));
        col.addView(Ui.spaceH(ctx, 10));
        TextView t = PenjualUi.text(ctx, label, 3f, 0xDD000000, false);
        t.setGravity(Gravity.CENTER);
        col.addView(t, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        Ui.onTap(col, onTap);
        col.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 17.5f), Ui.WRAP));
        return col;
    }
}
