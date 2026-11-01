package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.SplashScreenActivity;
import id.laukpauk.view.penjual.TokoSayaActivity;

/** Port of lib/views/pembeli/ResponseRPD.dart. Args: dataPenjual (object), dataRPD (object). */
public class ResponseRPDActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        float hb = Ui.hb(this);

        JsonObject dataPenjual = Json.object(args(), "dataPenjual");
        JsonObject dataRPD = Json.object(args(), "dataRPD");

        String namaToko = Json.str(dataPenjual, "namaToko");
        String statusRPD = Json.str(dataRPD, "status");
        String statusRPDLC = statusRPD.toLowerCase();

        String imgURL = statusRPDLC.equals("pending") ? "RPD_Pending.png" : "RPD_Rejected.png";
        boolean isApproved = statusRPDLC.equals("approved");

        String statusReason = statusRPDLC.equals("rejected") ? Json.str(dataRPD, "rejectionReason") : "";
        String nomorRekeningRPD = Json.str(dataRPD, "nomorRekening");
        long nominalRPD = Json.asLong(Json.get(dataRPD, "nominal"), 0);
        String namaPenerimaRPD = Json.str(dataRPD, "namaPenerima");

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(DetailUi.appBar(this, "Response Pencairan Dana"), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout body = Ui.column(this);
        body.setGravity(Gravity.CENTER);
        Ui.padXY(body, 15, 15);

        body.addView(Ui.asset(this, imgURL, hb * 30f));
        body.addView(Ui.spaceH(this, 25));
        body.addView(Ui.text(this, "Hai, Penjual " + namaToko,
                new Txt.Style(hb * 5f).color(CustomColors.nearlyBlack).bold()));
        body.addView(Ui.spaceH(this, 10));
        body.addView(centered(Ui.text(this, "Request Pencairan Dana",
                new Txt.Style(hb * 4.25f).color(CustomColors.textMuted))));
        body.addView(centered(Ui.text(this, Currency.formatRupiah(nominalRPD, 0),
                new Txt.Style(hb * 3.50f).color(CustomColors.green))));
        body.addView(Ui.spaceH(this, 7.5f));
        if (isApproved) {
            body.addView(centered(Ui.text(this,
                    "LaukPauk sudah melakukan penyetujuan terhadap proses pencairan dana anda, silahkan cek rekening anda.",
                    14f, 0xDD000000)));
        } else {
            body.addView(centered(Ui.text(this,
                    "Mohon maaf, permintaan anda untuk pencairan dana ditolak. Berikut alasan yang LaukPauk berikan :",
                    14f, 0xDD000000)));
        }
        if (!isApproved) {
            TextView reason = Ui.text(this, statusReason, hb * 3f, CustomColors.textMuted);
            reason.setGravity(Gravity.CENTER);
            Ui.padXY(reason, 0, 10);
            reason.setBackground(Ui.shape(this, 0x44DC3545, 10f));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            lp.topMargin = Ui.dp(this, 10);
            body.addView(reason, lp);
        } else {
            LinearLayout transferred = Ui.column(this);
            Ui.pad(transferred, 0, 7.5f, 0, 0);
            transferred.addView(centered(Ui.text(this, "LaukPauk telah mentransfer dana sebesar "
                    + Currency.formatRupiah(nominalRPD, 0) + " ke nomor rekening " + nomorRekeningRPD
                    + " AN Sdr. " + namaPenerimaRPD, 14f, 0xDD000000)));
            body.addView(transferred, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        body.addView(Ui.spaceH(this, 20));

        LinearLayout buttons = Ui.row(this);
        buttons.setGravity(Gravity.CENTER);
        buttons.addView(CustomButton.button(this, "Halaman Awal",
                () -> Nav.pushAndRemoveUntil(this, SplashScreenActivity.class, null),
                new CustomButton.Opts().pill().color(CustomButton.themeColor).outline()));
        buttons.addView(Ui.spaceW(this, 7.5f));
        buttons.addView(CustomButton.button(this, "Toko Saya",
                () -> Nav.pushAndRemoveUntil(this, TokoSayaActivity.class, null),
                new CustomButton.Opts().pill().color(CustomButton.themeColor)));
        body.addView(buttons, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        root.addView(body, Ui.weightH(1f));
        setRoot(root);
    }

    private TextView centered(TextView t) {
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        return t;
    }
}
