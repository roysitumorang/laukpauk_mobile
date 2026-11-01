package id.laukpauk.view.pembeli.components.detailproduk;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of lib/views/pembeli/components/DetailProduk/PilihanSatuanProduk.dart. */
public class PilihanSatuanProduk extends LinearLayout {
    private final String idProduk;
    private final Async.Done<JsonObject> onSatuanAndQuantitySelected;

    private JsonObject satuanDanQuantitySelectedState;
    private final List<PilihanSatuanProdukItem> items = new ArrayList<>();
    private final FrameLayout body;

    public PilihanSatuanProduk(Context ctx, String idProduk,
                               Async.Done<JsonObject> onSatuanAndQuantitySelected) {
        super(ctx);
        setOrientation(VERTICAL);
        this.idProduk = idProduk;
        this.onSatuanAndQuantitySelected = onSatuanAndQuantitySelected;

        body = new FrameLayout(ctx);
        addView(body, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        // FutureBuilder: waiting state
        LinearLayout waiting = Ui.column(ctx);
        Ui.pad(waiting, 0, 20, 0, 20);
        waiting.addView(DetailUi.smallSpinner(ctx, 15), new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 15)));
        body.addView(waiting, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        Async.Work<JsonArray> work = this::getHargaProduk;
        Async.Done<JsonArray> done = this::render;
        if (ctx instanceof BaseActivity) {
            ((BaseActivity) ctx).async(work, done);
        } else {
            Async.run(work, done);
        }
    }

    private JsonArray getHargaProduk() {
        String params = HTTPRequest.getQueryString("idProduk=" + idProduk);

        Http.Response httpResponse = Http.getApi("/Produk/getHargaProduk?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "hargaProduk");
        }
        return new JsonArray();
    }

    private void render(JsonArray listHargaProduk) {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        body.removeAllViews();
        items.clear();
        if (listHargaProduk != null && listHargaProduk.size() >= 1) {
            LinearLayout column = Ui.column(ctx);
            for (int i = 0; i < listHargaProduk.size(); i++) {
                PilihanSatuanProdukItem item = new PilihanSatuanProdukItem(ctx,
                        Json.objAt(listHargaProduk, i), satuanDanQuantitySelectedState,
                        returnedSatuanDanQuantityTerpilih -> {
                            if (onSatuanAndQuantitySelected != null) {
                                onSatuanAndQuantitySelected.run(returnedSatuanDanQuantityTerpilih);
                            }
                            satuanDanQuantitySelectedState = returnedSatuanDanQuantityTerpilih;
                            for (PilihanSatuanProdukItem other : items) {
                                other.setSatuanTerpilih(satuanDanQuantitySelectedState);
                            }
                        });
                items.add(item);
                column.addView(item, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            body.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            empty.addView(Ui.asset(ctx, "empty.png", hb * 25));
            empty.addView(Ui.spaceH(ctx, 5));
            empty.addView(Ui.text(ctx, "Mohon maaf, produk ini tidak memiliki daftar harga", hb * 3,
                    CustomColors.textMuted));
            empty.addView(Ui.spaceH(ctx, 15));
            body.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }
}
