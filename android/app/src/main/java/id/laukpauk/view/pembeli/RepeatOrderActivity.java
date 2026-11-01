package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.TransaksiModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.detailtransaksi.TransaksiUi;
import id.laukpauk.view.pembeli.components.repeatorder.RepeatOrderItemTransaksi;

/** Port of lib/views/pembeli/RepeatOrder.dart. Args: idTransaksi (String). */
public class RepeatOrderActivity extends BaseActivity {
    private String idTransaksi;
    private boolean isReadyToShow = false;
    private JsonElement detailTransaksiState = null; // null = Dart `false`

    private FrameLayout lowerHolder;
    private final AppState.Listener stateListener = this::renderLower;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idTransaksi = Json.str(args(), "idTransaksi");
        render();
        initialization();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppState.get().addListener(stateListener);
    }

    @Override
    protected void onStop() {
        AppState.get().removeListener(stateListener);
        super.onStop();
    }

    private void initialization() {
        final String id = idTransaksi;
        async(() -> TransaksiModel.getDetailTransaksi(id, "nomorTransaksi, tanggalDelivery"), detailTransaksi -> {
            isReadyToShow = true;
            detailTransaksiState = (detailTransaksi == null || Json.isFalse(detailTransaksi)) ? null
                    : detailTransaksi;
            render();
        });
    }

    private void render() {
        float hb = Ui.hb(this);
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);

        String nomorTransaksi = "#";
        if (detailTransaksiState != null) {
            nomorTransaksi = "#" + Json.str(Json.obj(detailTransaksiState), "nomorTransaksi");
        }

        if (isReadyToShow) {
            LinearLayout body = Ui.column(this);
            body.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            body.addView(Ui.spaceH(this, 5));

            LinearLayout pad = Ui.column(this);
            Ui.padXY(pad, 5, 0);
            LinearLayout card = TransaksiUi.card(this, 12.5f, 15f);
            card.addView(TransaksiUi.hbText(this, "Repeat Order", 3.75f, TransaksiUi.BLACK87, true));
            card.addView(TransaksiUi.hbText(this, "Nomor Transaksi " + nomorTransaksi, 3.0f, TransaksiUi.BLACK87,
                    false));
            card.addView(Ui.spaceH(this, 5.0f));
            card.addView(TransaksiUi.hbText(this,
                    "Anda dapat memesan kembali produk - produk yang sudah pernah anda beli sebelumnya. Silahkan pilih produk produk yang ingin anda beli ulang.",
                    2.85f, TransaksiUi.BLACK87, false));
            card.addView(Ui.spaceH(this, 7.5f));
            card.addView(TransaksiUi.hbText(this,
                    "NOTE : Harga dan diskon yang tertera pada tiap - tiap produk dalam transaksi ini bisa saja berbeda dengan harga dan diskon pada produk yang sekarang. Silahkan cek dahulu sebelum checkout di keranjang setelah anda memasukkan belanjaan ke keranjang.",
                    2.85f, CustomColors.textInfo, false));
            pad.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            body.addView(pad, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            lowerHolder = new FrameLayout(this);
            body.addView(lowerHolder, Ui.weightH(1f));
            renderLower();

            root.addView(body, Ui.weightH(1f));
        } else {
            FrameLayout center = new FrameLayout(this);
            center.addView(DetailUi.smallSpinner(this, 25f), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            root.addView(center, Ui.weightH(1f));
            lowerHolder = null;
        }

        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);
    }

    /** Consumer<HeaderViewModel>: RepeatOrderItemTransaksi when the address / schedule is set, else the prompt. */
    private void renderLower() {
        if (lowerHolder == null) {
            return;
        }
        float hb = Ui.hb(this);
        lowerHolder.removeAllViews();
        boolean isADJDSet = AppState.get().isADJDSet();

        if (isADJDSet) {
            lowerHolder.addView(new RepeatOrderItemTransaksi(this, idTransaksi, this::initialization),
                    new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        } else {
            LinearLayout col = Ui.column(this);
            col.setGravity(Gravity.CENTER);
            Ui.padXY(col, hb * 12.5f, 0);
            col.addView(Ui.asset(this, "adjd-belum-ditentukan.png", hb * 30));
            col.addView(Ui.spaceH(this, 10));
            TextView t = TransaksiUi.hbText(this,
                    "Tentukan Alamat dan Jadwal Delivery dulu ya mak, supaya bisa belanja ulang :)", 3.5f,
                    CustomColors.textMuted, false);
            t.setGravity(Gravity.CENTER);
            col.addView(t);
            col.addView(Ui.spaceH(this, 15));
            View btn = CustomButton.button(this, "Tentukan Lokasi dan Jadwal Delivery", () -> {
                HeaderModel.adjd(this, null, null, null, (JsonObject adjdSet) -> {
                    String latitude = Json.strOrNull(adjdSet, "latitude");
                    String longitude = Json.strOrNull(adjdSet, "longitude");
                    String jadwalDeliveryCode = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

                    boolean isADJDCompleted = false;
                    if (latitude != null && longitude != null && jadwalDeliveryCode != null) {
                        isADJDCompleted = true;
                    }
                    AppState.get().setADJDSet(isADJDCompleted);
                });
            }, new CustomButton.Opts().color(CustomButton.themeColor).pill());
            col.addView(btn, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            lowerHolder.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        }
    }
}
