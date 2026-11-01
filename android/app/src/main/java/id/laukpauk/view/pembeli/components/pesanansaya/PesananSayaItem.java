package id.laukpauk.view.pembeli.components.pesanansaya;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.PesananSayaModel;
import id.laukpauk.model.SettingModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailTransaksiActivity;
import id.laukpauk.view.pembeli.RepeatOrderActivity;
import id.laukpauk.view.pembeli.components.detailtransaksi.StatusTransaksi;
import id.laukpauk.view.pembeli.components.detailtransaksi.TransaksiUi;

/** Port of lib/views/pembeli/components/PesananSaya/PesananSayaItem.dart. */
public class PesananSayaItem extends LinearLayout {
    private final BaseActivity activity;
    private final JsonObject riwayat;
    private final Async.Done<Boolean> pesananDiterimaCallback;

    /** Dart: PesananSayaItem(riwayatTransaksi, {required pesananDiterimaCallback}). */
    public PesananSayaItem(Context ctx, JsonObject riwayatTransaksi, Async.Done<Boolean> pesananDiterimaCallback) {
        super(ctx);
        setOrientation(VERTICAL);
        this.activity = (BaseActivity) ctx;
        this.riwayat = riwayatTransaksi;
        this.pesananDiterimaCallback = pesananDiterimaCallback;
        render();
    }

    private static int num(JsonObject o, String key) {
        return Json.parseInt(Json.str(o, key), 0);
    }

    private void render() {
        removeAllViews();
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);

        final String idTransaksi = Json.str(riwayat, "id");

        String dayDelivery = Json.str(riwayat, "dayDelivery");
        final String status = Json.str(riwayat, "status");
        String jadwalDelivery = Json.str(riwayat, "jadwalDelivery");
        String waktuCheckoutFormatted = Json.str(riwayat, "waktuCheckout");
        final String namaMetodePembayaran = Json.str(riwayat, "namaMetodePembayaran");
        String metodePembayaran = Json.str(riwayat, "metodePembayaran");
        String namaMetodePembayaranLC = namaMetodePembayaran.toLowerCase();
        final String nomorTransaksi = Json.str(riwayat, "nomorTransaksi");

        int totalBelanja = num(riwayat, "totalBelanja");
        int ongkir = num(riwayat, "ongkir");

        int potonganWallet = num(riwayat, "potonganWallet");

        int nilaiVoucher = num(riwayat, "nilaiVoucher");
        int nilaiPointOut = num(riwayat, "pointOut");
        int nilaiDiskonKategori = num(riwayat, "diskonKategori");
        int diskonKodePromo = num(riwayat, "diskonPromo");

        int totalBelanjaBersih = (totalBelanja + ongkir)
                - (nilaiVoucher + potonganWallet + nilaiPointOut + nilaiDiskonKategori + diskonKodePromo);

        if (metodePembayaran.toLowerCase().equals("tf_bca")) {
            int kodeUnik = num(riwayat, "moreDataMetodePembayaran");
            totalBelanjaBersih += kodeUnik;
        }

        String statusReadable = PesananSayaModel.readAbleStatusForUser(status);
        final String statusLC = status.toLowerCase();
        boolean rejected = statusLC.equals("rejected");

        // InkWell > Card(margin 4, elevation 5, radius 10) > Container(decoration, padding 15)
        LinearLayout cardOuter = Ui.column(ctx);
        cardOuter.setBackground(Ui.shape(ctx, CustomColors.white, 10f));
        cardOuter.setElevation(Ui.dp(ctx, 5));
        LinearLayout.LayoutParams outerLp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        int m = Ui.dp(ctx, 4);
        outerLp.setMargins(m, m, m, m);

        LinearLayout inner = Ui.column(ctx);
        inner.setBackground(Ui.shape(ctx, rejected ? 0x25DC3545 : CustomColors.nearlyWhite, 10f,
                rejected ? CustomColors.textMuted : CustomColors.nearlyWhite, 1.5f));
        Ui.pad(inner, 15, 15, 15, 15);
        cardOuter.addView(inner, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        Layouts.ripple(cardOuter, 0x33000000, 10f);
        cardOuter.setOnClickListener(v -> Nav.push(ctx, DetailTransaksiActivity.class,
                Json.of("idTransaksi", idTransaksi)));

        // Row 1
        LinearLayout row1 = Ui.row(ctx);
        LinearLayout left = Ui.column(ctx);
        left.addView(TransaksiUi.hbText(ctx, "No. Invoice", 2.5f, CustomColors.textMuted, false));
        left.addView(Ui.spaceH(ctx, 2.5f));
        left.addView(TransaksiUi.hbText(ctx, nomorTransaksi, 3f, CustomColors.textMuted, true));
        left.addView(Ui.spaceH(ctx, 5f));
        left.addView(TransaksiUi.hbText(ctx, namaMetodePembayaran.toUpperCase(), 3f, CustomColors.textMuted, true));
        row1.addView(left, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        LinearLayout right = Ui.column(ctx);
        right.setGravity(Gravity.END);
        android.widget.TextView eta = TransaksiUi.hbText(ctx,
                dayDelivery.toUpperCase() + ", ETA jam " + jadwalDelivery, 3f, TransaksiUi.BLACK87, true);
        eta.setGravity(Gravity.END);
        right.addView(eta);
        right.addView(Ui.spaceH(ctx, 2.5f));
        android.widget.TextView waktu = TransaksiUi.hbText(ctx, waktuCheckoutFormatted, 2.75f,
                CustomColors.textMuted, true);
        waktu.setGravity(Gravity.END);
        right.addView(waktu);
        row1.addView(right, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        inner.addView(row1, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        inner.addView(Ui.spaceH(ctx, 20));

        // Row 2
        LinearLayout row2 = Ui.row(ctx);
        row2.setGravity(Gravity.CENTER_VERTICAL);
        row2.addView(TransaksiUi.hbText(ctx, "Status Pesanan : " + statusReadable, 3.0f, TransaksiUi.BLACK87, true),
                new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        android.widget.TextView harga = TransaksiUi.hbText(ctx, Currency.formatRupiah(totalBelanjaBersih, 0), 5f,
                rejected ? CustomColors.textMuted : CustomColors.green, true);
        harga.setGravity(Gravity.END);
        row2.addView(harga, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        inner.addView(row2, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (!rejected) {
            inner.addView(Ui.spaceH(ctx, 5f));
            LinearLayout row3 = Ui.row(ctx);
            row3.setGravity(Gravity.CENTER_VERTICAL);
            row3.addView(new StatusTransaksi(ctx, status, hb * 10, hb * 4.5, true, 10),
                    new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            row3.addView(Ui.spaceW(ctx, 10f));

            if (statusLC.equals("received")) {
                View btn = CustomButton.button(ctx, "Pesan Lagi",
                        () -> Nav.push(ctx, RepeatOrderActivity.class, Json.of("idTransaksi", idTransaksi)),
                        new CustomButton.Opts().size(CustomButton.smSize).color(CustomButton.themeColor).pill());
                row3.addView(btn, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
            }
            if (statusLC.equals("otw")) {
                View btn = CustomButton.button(ctx, "Diterima", () -> onDiterima(idTransaksi),
                        new CustomButton.Opts().size(CustomButton.smSize).color(CustomButton.themeColor).pill());
                row3.addView(btn, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
            }
            if (statusLC.equals("pending") && namaMetodePembayaranLC.equals("tf_bca")) {
                View btn = CustomButton.button(ctx, "Konfirmasi Bayar",
                        () -> onKonfirmasiBayar(nomorTransaksi, namaMetodePembayaran),
                        new CustomButton.Opts().size(CustomButton.smSize).color(CustomButton.infoColor).pill());
                row3.addView(btn, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
            }
            inner.addView(row3, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        addView(cardOuter, outerLp);
    }

    private void onDiterima(final String idTransaksi) {
        String title = "Pesanan Diterima";
        String subTitle = "Lakukan konfirmasi jika pesanan sudah diterima ya mak ...";
        Modal.Content<Boolean> actions = handle -> CustomAlert.actionsRow(activity,
                CustomAlert.textButton(activity, "Belum Diterima", () -> handle.close(Boolean.FALSE)),
                CustomAlert.textButton(activity, "OK", () -> handle.close(Boolean.TRUE)));
        CustomAlert.<Boolean>textComponent(activity, null, title, subTitle, actions, null, false, konfirmasi -> {
            if (konfirmasi != null && konfirmasi) {
                activity.async(() -> PesananSayaModel.pesananDiterima(idTransaksi), statusPesananDiterima -> {
                    if (pesananDiterimaCallback != null) {
                        pesananDiterimaCallback.run(statusPesananDiterima);
                    }
                });
            }
        });
    }

    private void onKonfirmasiBayar(final String nomorTransaksi, final String namaMetodePembayaran) {
        activity.async(SettingModel::getContactUsWhatsappConfiguration, (JsonElement contactUsWhatsapp) -> {
            // `contactUsWhatsapp != false`
            if (contactUsWhatsapp != null && !Json.isFalse(contactUsWhatsapp)) {
                String text = "Hai LaukPauk, saya ingin konfirmasi bahwa transaksi saya " + nomorTransaksi
                        + " sudah dibayar melalui " + namaMetodePembayaran
                        + ". Mohon untuk segera diproses. Terimakasih :)";
                String config = Json.str(Json.obj(contactUsWhatsapp), "contactUsWhatsapp");
                String url = "https://wa.me/" + config + "?text=" + Uri.encode(text);
                TransaksiUi.launch(activity, url);
            }
        });
    }
}
