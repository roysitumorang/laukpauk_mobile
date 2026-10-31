package id.laukpauk.view.pembeli.components.detailtransaksi;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.ClientPath;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of lib/views/pembeli/components/DetailTransaksi/ItemTransaksi.dart. */
public class ItemTransaksi extends LinearLayout {
    private final BaseActivity activity;
    private final String idTransaksi;
    private final String idPenjual;
    private final String cartCode;
    private final LinearLayout bodyHolder;

    /** Dart: ItemTransaksi(idTransaksi, {idPenjual, scaffold = false, cartCode}). */
    public ItemTransaksi(Context ctx, String idTransaksi, String idPenjual, boolean scaffold, String cartCode) {
        super(ctx);
        setOrientation(VERTICAL);
        this.activity = (BaseActivity) ctx;
        this.idTransaksi = idTransaksi;
        this.idPenjual = idPenjual;
        this.cartCode = cartCode;

        bodyHolder = Ui.column(ctx);
        if (scaffold) {
            addView(DetailUi.appBar(activity, "Daftar Belanjaan"), new LayoutParams(Ui.MATCH, Ui.WRAP));
            LinearLayout padded = Ui.column(ctx);
            Ui.padXY(padded, 5, 5);
            padded.addView(bodyHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));
            ScrollView sv = Ui.scroll(ctx, padded);
            addView(sv, Ui.weightH(1f));
        } else {
            addView(bodyHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        bodyHolder.addView(Loading.view(ctx));
        activity.async(this::getItemTransaksi, items -> {
            bodyHolder.removeAllViews();
            bodyHolder.addView(buildCard(items), new LayoutParams(Ui.MATCH, Ui.WRAP));
        });
    }

    public ItemTransaksi(Context ctx, String idTransaksi) {
        this(ctx, idTransaksi, null, false, null);
    }

    /** BLOCKING: GET /Transaksi/getItemTransaksiFromTransaksi/{id}. */
    private JsonArray getItemTransaksi() {
        String entitasItemTransaksi =
                "id,  namaProduk, namaSatuan, quantity, harga, diskon, catatan, jenisProduk, status, diskonPromo";
        String idPenjualQS = (idPenjual == null) ? "" : "&idPenjual=" + idPenjual;

        String params = id.laukpauk.core.HTTPRequest.getQueryString(
                "entitasItemTransaksi=" + entitasItemTransaksi + idPenjualQS, true);

        Http.Response httpResponse = Http.getApi(
                "/Transaksi/getItemTransaksiFromTransaksi/" + idTransaksi + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "itemTransaksi");
        }

        return new JsonArray();
    }

    private View buildCard(JsonArray itemTransaksi) {
        final Context ctx = getContext();
        LinearLayout card = TransaksiUi.card(ctx, 15, 10);

        if (itemTransaksi.size() >= 1) {
            // Row(Daftar Belanjaan, Invoice)
            LinearLayout head = Ui.row(ctx);
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(TransaksiUi.title(ctx, "Daftar Belanjaan"), new LayoutParams(0, Ui.WRAP, 1f));
            if (idPenjual != null) {
                TextView invoice = TransaksiUi.content(ctx, "Invoice", CustomColors.textPrimary);
                Ui.onTap(invoice, () -> {
                    String url = ServerPath.apiBaseURL + "/Transaksi/invoice/" + idTransaksi
                            + "?idPenjual=" + idPenjual;
                    if (!TransaksiUi.launch(activity, url)) {
                        showUrlTidakValid();
                    }
                });
                head.addView(invoice, new LayoutParams(Ui.WRAP, Ui.WRAP));
            }
            card.addView(head, new LayoutParams(Ui.MATCH, Ui.WRAP));
            card.addView(Ui.spaceH(ctx, 15));

            LinearLayout items = Ui.column(ctx);
            for (int i = 0; i < itemTransaksi.size(); i++) {
                items.addView(itemRow(Json.objAt(itemTransaksi, i)), new LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            items.addView(new GiftTransaksiPembeli(ctx, null, cartCode, false, false),
                    new LayoutParams(Ui.MATCH, Ui.WRAP));
            card.addView(items, new LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            int hPad = Math.round(Ui.hb(ctx) * 25);
            empty.setPadding(Ui.dp(ctx, hPad), 0, Ui.dp(ctx, hPad), 0);
            LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            int vm = Ui.dp(ctx, Ui.hb(ctx) * 5.0f);
            elp.setMargins(0, vm, 0, vm);

            ImageView img = Ui.asset(ctx, "empty.png", Ui.hb(ctx) * 20);
            empty.addView(img);
            empty.addView(Ui.spaceH(ctx, 5));
            TextView t = TransaksiUi.hbText(ctx, "Transaksi ini tidak memiliki item belanjaan", 3f,
                    CustomColors.textMuted, false);
            t.setGravity(Gravity.CENTER);
            empty.addView(t, new LayoutParams(Ui.WRAP, Ui.WRAP));
            empty.addView(Ui.spaceH(ctx, 10));
            card.addView(empty, elp);
        }
        return card;
    }

    private void showUrlTidakValid() {
        // SnackBar(content: Text('URL Tidak Valid !'), duration: 1 second)
        activity.toast("URL Tidak Valid !");
    }

    private View itemRow(JsonObject item) {
        Context ctx = getContext();
        int harga = Json.parseInt(Json.str(item, "harga"), 0);
        int diskon = Json.parseInt(Json.str(item, "diskon"), 0);
        int diskonPromo = Json.parseInt(Json.str(item, "diskonPromo"), 0);

        diskon = diskon + diskonPromo;

        int quantity = Json.parseInt(Json.str(item, "quantity"), 0);
        int totalHarga = quantity * (harga - diskon);

        String jenisProduk = Json.str(item, "jenisProduk");
        String namaSatuan;
        if (jenisProduk.toLowerCase().equals("nonpaket")) {
            namaSatuan = Json.str(item, "namaSatuan");
        } else {
            namaSatuan = "paket";
        }

        String status = Json.str(item, "status");
        String statusReadable = "Sedang di proses";
        int statusReadbleColor = CustomColors.textMuted;
        if (status.toLowerCase().equals("ok")) {
            statusReadable = "Produk Ada";
            statusReadbleColor = CustomColors.textSuccess;
        }

        boolean isRejected = status.toLowerCase().equals("rejected");

        if (isRejected) {
            statusReadable = "Produk Tidak Tersedia";
            statusReadbleColor = CustomColors.textDanger;
        }

        // `item['namaProduk'] ?? 'Produk Tidak Dikenal' + ' ' + quantity + ' x ' + namaSatuan`
        String namaProduk = Json.has(item, "namaProduk") ? Json.str(item, "namaProduk")
                : "Produk Tidak Dikenal" + " " + quantity + " x " + namaSatuan;

        String catatan = Json.str(item, "catatan");

        LinearLayout col = Ui.column(ctx);
        Ui.pad(col, 0, 0, 0, 10);

        LinearLayout row = Ui.row(ctx);
        row.addView(TransaksiUi.content(ctx, namaProduk, CustomColors.nearlyBlack), new LayoutParams(0, Ui.WRAP, 1f));
        row.addView(Ui.spaceW(ctx, 5));
        TextView total = TransaksiUi.content(ctx, Currency.formatRupiah(totalHarga, 0),
                isRejected ? CustomColors.textMuted : CustomColors.nearlyBlack, false, false, isRejected);
        total.setGravity(Gravity.END);
        row.addView(total, new LayoutParams(Ui.WRAP, Ui.WRAP));
        col.addView(row, new LayoutParams(Ui.MATCH, Ui.WRAP));

        col.addView(TransaksiUi.catatan(ctx, statusReadable, statusReadbleColor));
        col.addView(Ui.spaceH(ctx, 3.5f));
        if (catatan.length() >= 1) {
            col.addView(Ui.spaceH(ctx, 3));
            TextView c = TransaksiUi.catatan(ctx, "Catatan : " + catatan, CustomColors.nearlyBlack);
            c.setBackground(Ui.shape(ctx, 0x5566CC99, 10f));
            Ui.padXY(c, 10, 5);
            col.addView(c, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        return col;
    }
}
