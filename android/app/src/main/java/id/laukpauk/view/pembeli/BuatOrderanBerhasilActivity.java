package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;

/**
 * Port of lib/views/pembeli/BuatOrderanBerhasil.dart.
 * Args: returnedDataFromCheckout (object), removeTransactionSession (bool, default false).
 */
public class BuatOrderanBerhasilActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        final JsonObject returnedDataFromCheckout = Json.object(args(), "returnedDataFromCheckout");
        boolean removeTransactionSession = Json.bool(args(), "removeTransactionSession", false);
        if (removeTransactionSession) {
            TransaksiModel.removeTransactionSession(this, null);
        }

        float hb = Ui.hb(this);
        setStatusBarScrimColor(CustomColors.nearlyWhite);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.setGravity(Gravity.CENTER);

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(column, 15, 25, 15, 25);
        root.addView(column, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        column.addView(Ui.asset(this, "buatOrderanBerhasil.png", hb * 50));
        column.addView(Ui.spaceH(this, 15));
        TextView sub = Ui.text(this, "Yeayy! Orderan berhasil. Pesanan sedang diproses ya mak...!", hb * 3.75f,
                CustomColors.black);
        sub.setGravity(Gravity.CENTER);
        Ui.pad(sub, hb * 19.0f, 0, hb * 19.0f, 0);
        column.addView(sub, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(this, 25.0f));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        buttons.addView(CustomButton.button(this, "Belanja Lagi",
                () -> Nav.pushAndRemoveUntil(this, SplashScreenActivity.class, null),
                new CustomButton.Opts().color(CustomButton.themeColor).pill()));
        buttons.addView(Ui.spaceW(this, 7.5f));
        buttons.addView(CustomButton.button(this, "Lihat Detail Transaksi", () -> {
            JsonObject responseCheckout = Json.object(returnedDataFromCheckout, "responseCheckout");
            String idTransaksi = Json.str(responseCheckout, "idTransaksi");
            Nav.push(this, DetailTransaksiActivity.class, Json.of("idTransaksi", idTransaksi));
        }, new CustomButton.Opts().color(CustomButton.themeColor).pill().outline()));
        column.addView(buttons, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        setRoot(root);
    }
}
