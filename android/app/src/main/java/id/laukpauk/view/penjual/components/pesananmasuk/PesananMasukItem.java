package id.laukpauk.view.penjual.components.pesananmasuk;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.PesananSayaModel;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.detailtransaksi.StatusTransaksi;
import id.laukpauk.view.penjual.PesananScreenUi;
import id.laukpauk.view.penjual.ProsesPesananActivity;

/** Port of lib/views/penjual/components/PesananMasuk/PesananMasukItem.dart. */
public class PesananMasukItem extends LinearLayout {
    public PesananMasukItem(Context ctx, JsonObject riwayatTransaksi) {
        super(ctx);
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);
        JsonObject riwayat = riwayatTransaksi;

        final String idTransaksi = Json.str(riwayat, "id");

        String dayDelivery = Json.str(riwayat, "dayDelivery");
        String status = Json.str(riwayat, "status");
        String jadwalDelivery = Json.str(riwayat, "jadwalDelivery");
        String waktuCheckoutFormatted = Json.str(riwayat, "waktuCheckout");
        String namaMetodePembayaran = Json.str(riwayat, "namaMetodePembayaran");
        String nomorTransaksi = Json.str(riwayat, "nomorTransaksi");

        String jenisVoucher = Json.str(riwayat, "jenisVoucher");
        int nilaiVoucher = Json.parseInt(Json.str(riwayat, "nilaiVoucher"), 0);

        int totalBelanja = Json.parseInt(Json.str(riwayat, "totalBelanja"), 0);
        int ongkir = Json.parseInt(Json.str(riwayat, "ongkir"), 0);

        if (jenisVoucher.toLowerCase().equals("belanja")) {
            totalBelanja = totalBelanja - nilaiVoucher;
        }
        if (jenisVoucher.toLowerCase().equals("ongkir")) {
            ongkir = ongkir - nilaiVoucher;
        }

        int potonganWallet = Json.parseInt(Json.str(riwayat, "potonganWallet"), 0);

        int totalBelanjaBersih = (totalBelanja + ongkir) - potonganWallet;
        String statusReadable = PesananSayaModel.readAbleStatusForUser(status);
        boolean rejected = status.toLowerCase().equals("rejected");

        // Card(elevation: 5, radius 10) > Container(decoration: color + border 1.5, padding 15)
        LinearLayout card = PesananScreenUi.card(ctx);
        addView(card);
        LinearLayout inner = Ui.column(ctx);
        inner.setBackground(Ui.shape(ctx, rejected ? 0x25DC3545 : CustomColors.nearlyWhite, 10f,
                rejected ? CustomColors.textMuted : CustomColors.nearlyWhite, 1.5f));
        Ui.padAll(inner, 15);
        card.addView(inner, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        // header row
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.TOP);
        inner.addView(header, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout left = Ui.column(ctx);
        left.addView(Ui.text(ctx, nomorTransaksi, hb * 3, CustomColors.textMuted, true));
        left.addView(Ui.spaceH(ctx, 2.5f));
        left.addView(Ui.text(ctx, "Payment " + namaMetodePembayaran.toUpperCase(), hb * 3, CustomColors.textMuted,
                true));
        header.addView(left, Ui.weightW(1f));

        LinearLayout right = Ui.column(ctx);
        right.setGravity(Gravity.END);
        right.addView(Ui.text(ctx, dayDelivery.toUpperCase() + ", ETA jam " + jadwalDelivery, hb * 3,
                CustomColors.black, true));
        right.addView(Ui.spaceH(ctx, 2.5f));
        right.addView(Ui.text(ctx, waktuCheckoutFormatted, hb * 2.75f, CustomColors.textMuted, true));
        header.addView(right, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

        inner.addView(Ui.spaceH(ctx, 20));

        LinearLayout statusRow = new LinearLayout(ctx);
        statusRow.setOrientation(HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        statusRow.addView(Ui.text(ctx, "Status Pesanan : " + statusReadable, hb * 3.5f, CustomColors.black, true),
                Ui.weightW(1f));
        statusRow.addView(Ui.text(ctx, Currency.formatRupiah(totalBelanjaBersih, 0), hb * 5,
                rejected ? CustomColors.textMuted : CustomColors.green, true));
        inner.addView(statusRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (!rejected) {
            inner.addView(Ui.spaceH(ctx, 5.0f));
            LinearLayout actionRow = new LinearLayout(ctx);
            actionRow.setOrientation(HORIZONTAL);
            actionRow.setGravity(Gravity.CENTER_VERTICAL);
            actionRow.addView(new StatusTransaksi(ctx, status, hb * 10, hb * 4.5, true, 10));
            actionRow.addView(Ui.spaceW(ctx, 10.0f));
            if (status.toLowerCase().equals("processing")) {
                actionRow.addView(CustomButton.button(ctx, "Proses",
                        () -> Nav.push(getContext(), ProsesPesananActivity.class,
                                Json.of("idTransaksi", idTransaksi)),
                        new CustomButton.Opts().size(CustomButton.smSize).color(CustomButton.themeColor).pill()),
                        Ui.weightW(1f));
            }
            inner.addView(actionRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }
}
