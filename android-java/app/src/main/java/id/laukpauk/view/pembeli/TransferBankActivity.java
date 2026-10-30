package id.laukpauk.view.pembeli;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;

/** Port of lib/views/pembeli/TransferBank.dart. Args: returnedDataFromCheckout (object). */
public class TransferBankActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TransaksiModel.removeTransactionSession(this, null);

        JsonObject returnedDataFromCheckout = Json.object(args(), "returnedDataFromCheckout");
        final JsonObject responseCheckout = Json.object(returnedDataFromCheckout, "responseCheckout");
        JsonObject paymentResponse = Json.object(responseCheckout, "paymentResponse");

        int totalBayar = Json.intVal(paymentResponse, "totalBayar");
        int kodeUnik = Json.intVal(paymentResponse, "kodeUnik");
        String besarDana = Currency.formatRupiah((long) totalBayar + kodeUnik, 0);
        final String nomorRekening = Json.str(paymentResponse, "nomorRekening");
        String pemilikNomorRekening = Json.str(paymentResponse, "pemilikNomorRekening");

        float hb = Ui.hb(this);
        setStatusBarScrimColor(CustomColors.nearlyWhite);

        final Txt.Style titleSize = new Txt.Style(hb * 4.25f).bold();
        final Txt.Style subTitleSize = new Txt.Style(hb * 3.25f);
        Txt.Style xsTextStyle = new Txt.Style(hb * 3).color(CustomColors.textMuted);

        final FrameLayout host = new FrameLayout(this);
        host.setBackgroundColor(CustomColors.nearlyWhite);

        LinearLayout root = Ui.column(this);
        root.setGravity(Gravity.CENTER);
        host.addView(root, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        LinearLayout column = Ui.column(this);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(column, 15, 25, 15, 25);
        root.addView(column, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        column.addView(Ui.asset(this, "bca.png", hb * 35));
        column.addView(Ui.spaceH(this, 20.0f));
        column.addView(Ui.text(this, "Orderan Berhasil Dibuat", titleSize));

        LinearLayout inner = Ui.column(this);
        inner.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(inner, hb * 7.5f, 5, hb * 7.5f, 0);
        column.addView(inner, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        TextView instruksi = Ui.text(this, "Silahkan lakukan transfer ke nomor rekening di bawah ini :", subTitleSize);
        instruksi.setGravity(Gravity.CENTER);
        inner.addView(instruksi, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        inner.addView(Ui.spaceH(this, 15.0f));

        TextView rekening = Ui.text(this, nomorRekening, titleSize);
        Ui.onTap(rekening, () -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("nomorRekening", nomorRekening));
            }
            id.laukpauk.view.pembeli.DetailUi.snackbar(host, "Tersalin !", hb * 3.25f, 3000L, null, 0, null);
        });
        inner.addView(rekening, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        inner.addView(Ui.spaceH(this, 2.5f));
        inner.addView(Ui.text(this, "A.N. " + pemilikNomorRekening, subTitleSize));
        inner.addView(Ui.spaceH(this, 15.0f));
        inner.addView(Ui.text(this, besarDana, new Txt.Style(hb * 5).color(CustomColors.green).bold()));
        inner.addView(Ui.spaceH(this, 2.5f));
        inner.addView(Ui.text(this, "Klik nomor rekening untuk salin", xsTextStyle));

        TextView warning = Ui.text(this,
                "Silahkan transfer sesuai dengan nominal.\nJika tidak sesuai, transaksi Anda tidak akan ter-proses",
                xsTextStyle.copy().color(CustomColors.nearlyBlack));
        warning.setGravity(Gravity.CENTER);
        warning.setBackground(Ui.shape(this, CustomColors.textWarning, 10f));
        Ui.padAll(warning, 10);
        LinearLayout.LayoutParams warnLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        warnLp.setMargins(0, Ui.dp(this, 15), 0, Ui.dp(this, 15));
        inner.addView(warning, warnLp);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        buttons.addView(CustomButton.button(this, "Belanja Lagi",
                () -> Nav.pushAndRemoveUntil(this, SplashScreenActivity.class, null),
                new CustomButton.Opts().color(CustomButton.themeColor).pill()));
        buttons.addView(Ui.spaceW(this, 7.5f));
        buttons.addView(CustomButton.button(this, "Detail Transaksi", () -> {
            String idTransaksi = Json.str(responseCheckout, "idTransaksi");
            Nav.push(this, DetailTransaksiActivity.class, Json.of("idTransaksi", idTransaksi));
        }, new CustomButton.Opts().color(CustomButton.themeColor).pill().outline()));
        inner.addView(buttons, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        setRoot(host);
    }
}
