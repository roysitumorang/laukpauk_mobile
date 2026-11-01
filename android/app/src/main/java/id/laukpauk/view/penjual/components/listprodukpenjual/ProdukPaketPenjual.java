package id.laukpauk.view.penjual.components.listprodukpenjual;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.TambahProdukActivity;

/** Port of lib/views/penjual/components/ListProdukPenjual/ProdukPaketPenjual.dart (the "Produk Paketan" tab). */
public class ProdukPaketPenjual extends FrameLayout {
    public ProdukPaketPenjual(Context ctx) {
        super(ctx);
        showSpinner();
        load();
    }

    private BaseActivity activity() {
        return (BaseActivity) getContext();
    }

    private void showSpinner() {
        removeAllViews();
        Context ctx = getContext();
        LinearLayout box = Ui.column(ctx);
        Ui.padXY(box, 0, 25);
        box.addView(PenjualUi.spinner(ctx, 15), new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 15)));
        addView(box, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    /** Dart getProdukPaketPenjual(). BLOCKING. */
    private JsonArray getProdukPaketPenjual() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Penjual/getPaketPenjual?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "paketPenjual");
        }

        return new JsonArray();
    }

    /** setState(() { reload++ }): fetches the list again and rebuilds it (the old list stays until it arrives). */
    private void load() {
        PenjualUi.async(getContext(), () -> getProdukPaketPenjual(), produkPaketan -> render(produkPaketan));
    }

    private void render(JsonArray produkPaketan) {
        removeAllViews();
        final Context ctx = getContext();

        LinearLayout content = Ui.column(ctx);

        if (produkPaketan.size() >= 1) {
            for (JsonElement e : produkPaketan) {
                content.addView(new ProdukPaketPenjualItem(ctx, Json.obj(e), statusHapusPaket -> {
                    if (statusHapusPaket != null) {
                        if (statusHapusPaket) {
                            load();
                        } else {
                            PenjualUi.snack(activity(), "Gagal menghapus produk paket. Silahkan coba lagi !", 3, 2.5f);
                        }
                    }
                }, statusEditProduk -> {
                    if (statusEditProduk) {
                        load();
                    } else {
                        PenjualUi.snack(activity(), "Gagal memperbaharui data produk. Silahkan coba lagi !", 3, 2.5f);
                    }
                }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        } else {
            content.setGravity(Gravity.CENTER);
            content.addView(Ui.asset(ctx, "empty.png", Ui.hb(ctx) * 25));
            content.addView(Ui.spaceH(ctx, 5));
            content.addView(PenjualUi.text(ctx, "Anda belum memiliki produk PAKETAN apapun.", 3f, 0xDD000000, false));
            content.addView(PenjualUi.textButton(ctx, "Tambah Produk Baru", 14f, CustomColors.green, false, () ->
                    Nav.pushForResult(activity(), TambahProdukActivity.class, Json.of("jenisProduk", "satuan"),
                            result -> {
                                if (result != null && result.isJsonPrimitive()) {
                                    if (Json.asBool(result, false)) {
                                        load();
                                    } else {
                                        PenjualUi.snack(activity(),
                                                "Gagal menambah data produk baru. Silahkan coba lagi !", 3, 2.5f);
                                    }
                                }
                            })));
        }

        SwipeRefreshLayout refresh = DetailUi.refreshable(ctx, content, () -> load(), true);
        addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
