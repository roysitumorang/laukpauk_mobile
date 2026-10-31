package id.laukpauk.view.pembeli.components.useranddashboard;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import id.laukpauk.core.Json;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.AccountUi;
import id.laukpauk.view.pembeli.PesananSayaActivity;
import id.laukpauk.view.pembeli.ProfilSayaActivity;
import id.laukpauk.view.pembeli.RequestJadiPenjualActivity;
import id.laukpauk.view.penjual.TokoSayaActivity;

/** Port of lib/views/pembeli/components/UserAndDashboard/Transaksi.dart. */
public class Transaksi extends LinearLayout {

    public Transaksi(final Context ctx, JsonObject dataPenjual) {
        super(ctx);
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);
        Ui.padXY(this, 15, 0);

        final boolean isSeller = Json.str(dataPenjual, "isSeller").equals("1");

        addView(Ui.text(ctx, "Transaksi dan Akun", new Txt.Style(hb * 4.25f).bold()));
        addView(Ui.spaceH(ctx, 15));

        LinearLayout row = Ui.row(ctx);
        row.setGravity(Gravity.TOP);
        row.addView(item(ctx, "icons/pembeli/belanjaan-hari-ini.png", "Belanjaan Hari Ini",
                () -> Nav.push(ctx, PesananSayaActivity.class)));
        row.addView(AccountUi.filler(ctx));
        row.addView(item(ctx, "icons/pembeli/histori-belanja.png", "Histori Belanja Saya",
                () -> Nav.push(ctx, PesananSayaActivity.class, Json.of("statusTransaksi", 2))));
        row.addView(AccountUi.filler(ctx));
        row.addView(item(ctx, "store.png", isSeller ? "Lihat Toko Saya" : "Jualan Yuk!", () -> {
            if (isSeller) {
                Nav.pushAndRemoveUntil(ctx, TokoSayaActivity.class, null);
            } else {
                Nav.push(ctx, RequestJadiPenjualActivity.class);
            }
        }));
        row.addView(AccountUi.filler(ctx));
        row.addView(item(ctx, "user-icon.png", "Atur Akun Saya",
                () -> Nav.push(ctx, ProfilSayaActivity.class)));
        addView(row, new LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));
    }

    private static LinearLayout item(Context ctx, String image, String label, Runnable onTap) {
        float hb = Ui.hb(ctx);
        LinearLayout column = Ui.column(ctx);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.addView(Ui.asset(ctx, image, hb * 9f, hb * 9f));
        column.addView(Ui.spaceH(ctx, 10));
        android.widget.TextView text = Ui.text(ctx, label, hb * 3.25f, 0xDD000000);
        text.setGravity(Gravity.CENTER);
        column.addView(text, new LayoutParams(Ui.MATCH, Ui.WRAP));
        column.setLayoutParams(new LayoutParams(Ui.dp(ctx, hb * 20f), Ui.WRAP));
        Ui.onTap(column, onTap);
        return column;
    }
}
