package id.laukpauk.view.components.penjual;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.produkdidalampasar.ProdukDiDalamPasarItem;
import id.laukpauk.view.penjual.PenjualUi;

/** Port of lib/views/components/Penjual/ProdukPenjual.dart (the "Produk" tab of the seller storefront). */
public class ProdukPenjual extends LinearLayout {
    private final String idPenjual;

    private boolean isReadyToShow = false;
    private JsonArray produkPenjualState = new JsonArray();
    private JsonArray produkPenjualImmutableState = new JsonArray();
    private String pencarian = "";

    private FrameLayout body;

    public ProdukPenjual(Context ctx, String idPenjual) {
        super(ctx);
        this.idPenjual = idPenjual;
        setOrientation(VERTICAL);
        render();
        initialization();
    }

    private BaseActivity activity() {
        return (BaseActivity) getContext();
    }

    private JsonArray getProdukPenjual() {
        String getProductEntities = "nama, foto, id, namaToko, namaKategoriProduk, idPenjual, stokAvailable";

        String params = HTTPRequest.getQueryString("getProductEntities=" + getProductEntities);

        Http.Response httpResponse = Http.getApi("/Penjual/getProdukPenjual/" + idPenjual + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "produkPenjual");
        }
        return new JsonArray();
    }

    private void initialization() {
        PenjualUi.async(getContext(), () -> getProdukPenjual(), produkPenjual -> {
            isReadyToShow = true;
            produkPenjualState = produkPenjual;
            produkPenjualImmutableState = produkPenjual;
            render();
        });
    }

    private void deteksiADJD(final JsonObject produk) {
        final BaseActivity activity = activity();
        HeaderModel.adjd(activity, null, null, null, adjdSet -> {
            String latitude = Json.strOrNull(adjdSet, "latitude");
            String longitude = Json.strOrNull(adjdSet, "longitude");
            String jadwalDeliveryCode = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

            if (latitude != null && longitude != null && jadwalDeliveryCode != null) {
                ProdukModel.showSatuanProduk(activity, produk, v -> AppState.get().setADJDSet(true));
                return;
            }
            AppState.get().setADJDSet(false);
        });
    }

    private void onSearch(String string) {
        String stringLC = string.toLowerCase(Locale.ROOT);
        JsonArray filtered = new JsonArray();
        for (JsonElement e : produkPenjualImmutableState) {
            JsonObject produk = Json.obj(e);
            String namaProduk = Json.str(produk, "nama");
            String namaProdukLC = namaProduk.toLowerCase(Locale.ROOT);

            if (namaProdukLC.indexOf(stringLC) >= 0) {
                filtered.add(produk);
            }
        }
        produkPenjualState = filtered;
        pencarian = stringLC;
        renderBody();
    }

    private void onClose() {
        produkPenjualState = produkPenjualImmutableState;
        pencarian = "";
        renderBody();
    }

    private void render() {
        removeAllViews();
        body = null;
        Context ctx = getContext();

        if (!isReadyToShow) {
            addView(Loading.view(ctx), new LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        boolean hasItems = produkPenjualState.size() >= 1;
        View pencarianView = new Pencarian(ctx, string -> onSearch(string), () -> onClose(),
                hasItems ? "Cari Produk di Sini ..." : "Cari Produk di Sini", pencarian);
        addView(pencarianView, PenjualUi.lp(ctx, Ui.MATCH, Ui.WRAP, 0, 5, 0, 5));
        addView(Ui.spaceH(ctx, hasItems ? 5 : 15));

        body = new FrameLayout(ctx);
        addView(body, Ui.weightH(1f));
        renderBody();
    }

    private void renderBody() {
        if (body == null) {
            return;
        }
        body.removeAllViews();
        Context ctx = getContext();

        if (produkPenjualState.size() >= 1) {
            List<View> items = new ArrayList<>();
            for (JsonElement e : produkPenjualState) {
                final JsonObject produk = Json.obj(e);
                items.add(new ProdukDiDalamPasarItem(ctx, produk, () -> deteksiADJD(produk), null));
            }
            ScrollView scroll = PenjualUi.innerScroll(ctx);
            scroll.addView(Layouts.grid(ctx, items, 3, 0f, 0f, 0.5f), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            body.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        } else {
            LinearLayout col = PenjualUi.emptyState(ctx, 25f, 5f, "Tidak Ada Produk", 3.5f,
                    CustomColors.textMuted);
            col.setGravity(Gravity.CENTER);
            body.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH, Gravity.CENTER));
        }
    }
}
