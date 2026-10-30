package id.laukpauk.view;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.components.penjual.PaketPenjual;
import id.laukpauk.view.components.penjual.ProdukPenjual;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.pembeli.ProdukDiDalamPasarActivity;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.PesananScreenUi;

/**
 * Port of lib/views/Penjual.dart (public seller storefront).
 * Args: id (String), jenisProdukSelected (String, optional: "satuan" | "paket").
 */
public class PenjualActivity extends BaseActivity {
    private int jenisProdukSelected = 0;
    private String idPenjual;

    private FrameLayout content;
    private JsonElement detailPenjual;
    private FrameLayout produkHolder;
    private HorizontalScrollView tabHolderContent;
    private FrameLayout tabHolder;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idPenjual = Json.str(args(), "id");

        String jenisProdukSelectedString = Json.strOrNull(args(), "jenisProdukSelected");
        if (jenisProdukSelectedString != null) {
            jenisProdukSelected = jenisProdukSelectedString.equals("satuan") ? 0 : 1;
        }

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        content = new FrameLayout(this);
        root.addView(content, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        content.addView(Loading.view(this));
        load();
    }

    private JsonElement getDetailPenjual() {
        Http.Response httpResponse = Http.getApi("/Penjual/getDetailPenjual/" + idPenjual);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return dataObject.get("detailPenjual");
        }
        return null;
    }

    private void load() {
        async(() -> getDetailPenjual(), result -> {
            detailPenjual = result;
            render();
        });
    }

    private void render() {
        content.removeAllViews();
        if (detailPenjual == null || !Json.isObject(detailPenjual)) {
            content.addView(buildNotFound(), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }
        content.addView(buildPage(Json.obj(detailPenjual)), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private View buildNotFound() {
        LinearLayout col = Ui.column(this);
        col.setGravity(Gravity.CENTER);
        col.addView(Ui.asset(this, "empty.png", PenjualUi.hb(this, 30)));
        TextView t = PenjualUi.text(this, "Data Penjual tidak Ditemukan", 3f, CustomColors.textMuted, true);
        col.addView(t, PenjualUi.lp(this, Ui.WRAP, Ui.WRAP, 0, 15, 0, 15));
        return col;
    }

    private View buildPage(JsonObject detail) {
        String namaToko = Json.isNull(Json.get(detail, "namaToko")) ? "Toko Tidak Dikenal"
                : Json.str(detail, "namaToko");
        JsonElement namaPasarElement = Json.get(detail, "namaPasar");
        final String namaPasar = Json.isNull(namaPasarElement) ? "Belum Menentukan Pasar"
                : (Json.asStr(namaPasarElement, "").length() >= 1) ? Json.asStr(namaPasarElement, "")
                : "Pasar Tanpa Nama";

        String alamatToko = Json.isNull(Json.get(detail, "alamatToko")) ? "Alamat belum ditentukan"
                : Json.str(detail, "alamatToko");

        final String idPasar = Json.str(detail, "pasar");

        LinearLayout column = Ui.column(this);

        column.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout list = Ui.column(this);

        // Container(width: 100hb, height: 60hb, Image.network(banner, cover))
        ImageView banner = Ui.network(this, ServerPath.baseURL + "/" + ServerPath.uploadGambarBannerPenjual + "/"
                + Json.str(detail, "banner"), 100 * Ui.hb(this), 60 * Ui.hb(this));
        banner.setLayoutParams(new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 60 * Ui.hb(this))));
        list.addView(banner);
        list.addView(Ui.spaceH(this, 15));

        TextView nama = PenjualUi.text(this, namaToko, 5f, CustomColors.green, true);
        nama.setGravity(Gravity.CENTER);
        list.addView(nama, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        list.addView(Ui.spaceH(this, 5));

        TextView alamat = PenjualUi.text(this, alamatToko, 3f, CustomColors.grey, false);
        alamat.setGravity(Gravity.CENTER);
        list.addView(alamat, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        list.addView(Ui.spaceH(this, 3f * Ui.hb(this)));

        TextView pasar = PenjualUi.text(this, namaPasar, 3.5f, CustomColors.textInfo, true);
        pasar.setGravity(Gravity.CENTER);
        Ui.onTap(pasar, () -> Nav.push(this, ProdukDiDalamPasarActivity.class,
                Json.of("idPasar", idPasar, "namaPasar", namaPasar)));
        list.addView(pasar, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        TextView hint = PenjualUi.text(this, "Klik pasar untuk lihat produk di dalamnya", 2.75f,
                CustomColors.textMuted, false);
        hint.setGravity(Gravity.CENTER);
        list.addView(hint, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        list.addView(Ui.spaceH(this, 5));

        tabHolder = new FrameLayout(this);
        Ui.pad(tabHolder, 50, 0, 50, 0);
        list.addView(tabHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        renderTabs();
        list.addView(Ui.spaceH(this, 10));

        produkHolder = new FrameLayout(this);
        Ui.pad(produkHolder, 10, 0, 10, 0);
        list.addView(produkHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, Ui.vb(this) * 69f)));
        renderProduk();

        SwipeRefreshLayout refresh = DetailUi.refreshable(this, list, () -> {
            async(() -> getDetailPenjual(), result -> {
                detailPenjual = result;
                render();
            });
        }, false);
        refresh.setLayoutParams(Ui.weightH(1f));
        column.addView(refresh);
        return column;
    }

    private void renderTabs() {
        tabHolder.removeAllViews();
        tabHolder.addView(PesananScreenUi.tabBar(this, new String[]{"Produk", "Paket"}, jenisProdukSelected, 14f,
                value -> {
                    jenisProdukSelected = value;
                    renderTabs();
                    renderProduk();
                }));
    }

    private void renderProduk() {
        produkHolder.removeAllViews();
        View v = (jenisProdukSelected == 0) ? new ProdukPenjual(this, idPenjual) : new PaketPenjual(this, idPenjual);
        produkHolder.addView(v, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
