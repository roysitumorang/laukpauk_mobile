package id.laukpauk.view.penjual.components.tokosaya;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.penjual.ListProdukPenjualActivity;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.TambahProdukActivity;

/**
 * Port of lib/views/penjual/components/TokoSaya/ProdukPenjual.dart.
 * `saveProdukCallback` gets the result of TambahProduk (true / false) or null when it was dismissed.
 */
public class ProdukPenjual extends LinearLayout {
    private final Async.Done<Boolean> saveProdukCallback;

    private FrameLayout jumlahHolder;

    public ProdukPenjual(Context ctx, Async.Done<Boolean> saveProdukCallback) {
        super(ctx);
        this.saveProdukCallback = saveProdukCallback;
        setOrientation(VERTICAL);
        render();
        PenjualUi.async(ctx, () -> getJumlahProdukPenjual(), jumlah -> renderJumlah(jumlah));
    }

    /** Dart getJumlahProdukPenjual(): the `jumlahProduk` map, or null (= false). BLOCKING. */
    private JsonObject getJumlahProdukPenjual() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Penjual/getJumlahProdukPenjual?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            JsonElement jumlahProduk = dataObject.get("jumlahProduk");
            if (Json.isObject(jumlahProduk)) {
                return Json.obj(jumlahProduk);
            }
        }
        return null;
    }

    private void render() {
        final Context ctx = getContext();
        LinearLayout card = PenjualUi.cardPadded(ctx, 15, 10);
        addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        card.addView(PenjualUi.text(ctx, "Produk Saya", 4f, 0xDD000000, true));
        card.addView(Ui.spaceH(ctx, 15));

        jumlahHolder = new FrameLayout(ctx);
        jumlahHolder.addView(PenjualUi.spinner(ctx, 15), new FrameLayout.LayoutParams(Ui.dp(ctx, 15),
                Ui.dp(ctx, 15), Gravity.CENTER));
        Ui.pad(jumlahHolder, 0, 15, 0, 15);
        card.addView(jumlahHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        card.addView(Ui.spaceH(ctx, 10));

        LinearLayout buttons = Ui.row(ctx);
        buttons.setGravity(Gravity.CENTER);
        buttons.addView(PenjualUi.textButton(ctx, "Lihat Produk Saya", Ui.hb(ctx) * 3.5f, PenjualUi.defaultBlue, false,
                () -> Nav.push(ctx, ListProdukPenjualActivity.class)));
        buttons.addView(PenjualUi.textButton(ctx, "Tambah Produk Baru", Ui.hb(ctx) * 3.5f, PenjualUi.defaultBlue,
                false, () -> Nav.pushForResult((BaseActivity) ctx, TambahProdukActivity.class,
                        Json.of("jenisProduk", "satuan"), result -> {
                            Boolean statusSaveProduk = (result != null && result.isJsonPrimitive())
                                    ? Boolean.valueOf(Json.asBool(result, false)) : null;
                            saveProdukCallback.run(statusSaveProduk);
                        })));
        card.addView(buttons, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private void renderJumlah(JsonObject dataProduk) {
        final Context ctx = getContext();
        jumlahHolder.removeAllViews();
        Ui.pad(jumlahHolder, 0, 0, 0, 0);

        if (dataProduk == null) {
            jumlahHolder.addView(PenjualUi.plain(ctx, "Gagal mengambil data produk"),
                    new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            return;
        }

        View satuan = column(ctx, "Produk Satuan", Json.asInt(Json.get(dataProduk, "produk"), 0), () -> {
            String idUser = LocalStorage.getApplicationID();
            Nav.push(ctx, PenjualActivity.class, Json.of("id", idUser, "jenisProdukSelected", "satuan"));
        });
        View paket = column(ctx, "Produk Paketan", Json.asInt(Json.get(dataProduk, "paket"), 0), () -> {
            String idUser = LocalStorage.getCurrentUserID();
            Nav.push(ctx, PenjualActivity.class, Json.of("id", idUser, "jenisProdukSelected", "paket"));
        });
        jumlahHolder.addView(PenjualUi.spaceAround(ctx, satuan, paket),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private LinearLayout column(Context ctx, String title, int jumlah, Runnable onTap) {
        LinearLayout col = Ui.column(ctx);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.addView(PenjualUi.text(ctx, title, 3.5f, 0xDD000000, false));
        col.addView(Ui.spaceH(ctx, 5));
        View number = PenjualUi.text(ctx, Currency.formatSatuan(jumlah, ""), 7f, CustomColors.green, true);
        Ui.onTap(number, onTap);
        col.addView(number, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        col.addView(PenjualUi.text(ctx, "Produk", 3f, 0xDD000000, false));
        return col;
    }
}
