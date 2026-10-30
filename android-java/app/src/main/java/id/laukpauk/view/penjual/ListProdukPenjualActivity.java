package id.laukpauk.view.penjual;

import android.os.Bundle;
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
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.components.listprodukpenjual.ListProdukPenjualItem;
import id.laukpauk.view.penjual.components.listprodukpenjual.ProdukPaketPenjual;

/** Port of lib/views/penjual/ListProdukPenjual.dart (no arguments). */
public class ListProdukPenjualActivity extends BaseActivity {
    private int jenisProdukSelected = 0;

    private FrameLayout tabHolder;
    private FrameLayout body;

    /** Dart getProdukPenjual(). BLOCKING. */
    private JsonArray getProdukPenjual() {
        String getProductEntities = "id, nama, foto, namaKategoriProduk, keterangan";
        String params = HTTPRequest.getQueryString("getProductEntities=" + getProductEntities);

        Http.Response httpResponse = Http.getApi("/Penjual/getProdukPenjual?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "produkPenjual");
        }

        return new JsonArray();
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        // Padding(h5, v5) > Card(elevation 5, radius 10) > Padding(h15, v10)
        LinearLayout cardWrap = Ui.column(this);
        Ui.padXY(cardWrap, 5, 5);
        LinearLayout card = PenjualUi.card(this);
        Ui.padXY(card, 15, 10);
        cardWrap.addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(cardWrap, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout title = Ui.row(this);
        title.addView(PenjualUi.text(this, "Produk Anda", 3.5f, 0xDD000000, true));
        title.addView(Ui.spacer(this));
        TextView tambah = PenjualUi.text(this, "Tambah Produk Baru", 3f, CustomColors.textPrimary, true);
        Ui.onTap(tambah, () -> Nav.pushForResult(this, TambahProdukActivity.class, Json.of("jenisProduk", "satuan"),
                result -> {
                    if (result != null && result.isJsonPrimitive()) {
                        if (Json.asBool(result, false)) {
                            renderBody();
                        } else {
                            PenjualUi.snack(this, "Anda gagal menambahkan produk baru. Silahkan coba lagi !", 3,
                                    2.5f);
                        }
                    }
                }));
        title.addView(tambah);
        card.addView(title, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 5));
        card.addView(PenjualUi.text(this,
                "Silahkan atur produk anda. Anda bisa menambah, mengubah dan menghapus produk - produk anda.", 3f,
                0xDD000000, false));

        tabHolder = new FrameLayout(this);
        Ui.pad(tabHolder, 0, 0, 0, 5);
        root.addView(tabHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        renderTabs();

        body = new FrameLayout(this);
        root.addView(body, Ui.weightH(1f));
        renderBody();

        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);
    }

    private void renderTabs() {
        tabHolder.removeAllViews();
        tabHolder.addView(PesananScreenUi.tabBar(this, new String[]{"Produk Satuan", "Produk Paketan"},
                jenisProdukSelected, 14f, value -> {
                    jenisProdukSelected = value;
                    renderTabs();
                    renderBody();
                }), new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.START));
    }

    /** The Expanded child: the single products list (FutureBuilder) or ProdukPaketPenjual. */
    private void renderBody() {
        if (jenisProdukSelected != 0) {
            body.removeAllViews();
            body.addView(new ProdukPaketPenjual(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }
        if (body.getChildCount() == 0 || body.getChildAt(0) instanceof ProdukPaketPenjual) {
            body.removeAllViews();
            LinearLayout box = Ui.column(this);
            Ui.padXY(box, 0, 25);
            box.addView(PenjualUi.spinner(this, 25), new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 25)));
            body.addView(box, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        async(() -> getProdukPenjual(), produkPenjual -> {
            if (jenisProdukSelected == 0) {
                renderProduk(produkPenjual);
            }
        });
    }

    private void renderProduk(JsonArray produkPenjual) {
        body.removeAllViews();

        LinearLayout content = Ui.column(this);
        if (produkPenjual.size() >= 1) {
            for (JsonElement e : produkPenjual) {
                content.addView(new ListProdukPenjualItem(this, Json.obj(e), statusHapusProduk -> {
                    if (statusHapusProduk) {
                        renderBody();
                    } else {
                        PenjualUi.snack(this, "Gagal menghapus produk. Silahkan coba lagi !", 3, 2.5f);
                    }
                }, statusEditProduk -> {
                    if (statusEditProduk) {
                        renderBody();
                    } else {
                        PenjualUi.snack(this, "Gagal memperbaharui data produk. Silahkan coba lagi !", 3, 2.5f);
                    }
                }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        } else {
            LinearLayout col = Ui.column(this);
            col.setGravity(Gravity.CENTER);
            Ui.padXY(col, 0, 15);
            col.addView(Ui.asset(this, "empty.png", Ui.hb(this) * 25));
            col.addView(Ui.spaceH(this, 5));
            col.addView(PenjualUi.text(this, "Anda belum memiliki produk SATUAN apapun.", 3f, 0xDD000000, false));
            col.addView(PenjualUi.textButton(this, "Tambah Produk Baru", 14f, CustomColors.green, false, () ->
                    Nav.pushForResult(this, TambahProdukActivity.class, Json.of("jenisProduk", "satuan"),
                            result -> {
                                if (result != null && result.isJsonPrimitive()) {
                                    if (Json.asBool(result, false)) {
                                        renderBody();
                                    } else {
                                        PenjualUi.snack(this, "Gagal menambah data produk baru. Silahkan coba lagi !",
                                                3, 2.5f);
                                    }
                                }
                            })));
            content.setGravity(Gravity.CENTER);
            content.addView(col, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        SwipeRefreshLayout refresh = DetailUi.refreshable(this, content, () -> renderBody(), true);
        body.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
