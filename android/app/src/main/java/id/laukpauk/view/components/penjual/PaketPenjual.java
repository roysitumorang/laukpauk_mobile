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
import id.laukpauk.model.PaketModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.PenjualUi;

/** Port of lib/views/components/Penjual/PaketPenjual.dart (the "Paket" tab of the seller storefront). */
public class PaketPenjual extends LinearLayout {
    private final String idPenjual;

    private boolean isReadyToShow = false;
    private JsonArray paketPenjualState = new JsonArray();
    private JsonArray paketPenjualImmutableState = new JsonArray();
    private String pencarian = "";

    private Pencarian pencarianView;
    private FrameLayout body;

    public PaketPenjual(Context ctx, String idPenjual) {
        super(ctx);
        this.idPenjual = idPenjual;
        setOrientation(VERTICAL);
        render();
        initialization();
    }

    private BaseActivity activity() {
        return (BaseActivity) getContext();
    }

    private JsonArray getPaketPenjual() {
        String getPaketEntities = "nama, foto, id, harga, diskon, idPenjual";
        String params = HTTPRequest.getQueryString("getPaketEntities=" + getPaketEntities);

        Http.Response httpResponse = Http.getApi("/Penjual/getPaketPenjual/" + idPenjual + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "paketPenjual");
        }
        return new JsonArray();
    }

    private void initialization() {
        PenjualUi.async(getContext(), () -> getPaketPenjual(), paketPenjual -> {
            isReadyToShow = true;
            paketPenjualState = paketPenjual;
            paketPenjualImmutableState = paketPenjual;
            pencarianView = null;
            render();
        });
    }

    private void deteksiADJD(final JsonObject produk) {
        final BaseActivity activity = activity();
        HeaderModel.adjd(activity, null, null, null, adjdSet -> {
            String latitude = Json.strOrNull(adjdSet, "latitude");
            String longitude = Json.strOrNull(adjdSet, "longitude");
            String jadwalDeliveryCode = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

            boolean isADJDCompleted = false;
            if (latitude != null && longitude != null && jadwalDeliveryCode != null) {
                isADJDCompleted = true;
                final String idPaket = Json.str(produk, "id");
                final boolean completed = isADJDCompleted;
                activity.async(() -> PaketModel.addToCart(idPaket), statusAddToCart -> {
                    String text = (statusAddToCart != null && statusAddToCart)
                            ? "Produk berhasil ditambahkan ke keranjang !"
                            : "Produk gagal ditambahkan ke keranjang, silahkan coba lagi.";

                    PaketModel.addToCartSnackBar(activity, text);

                    activity.async(() -> HeaderModel.getJumlahCart(), jumlahKeranjang -> {
                        AppState.get().setJumlahCart(jumlahKeranjang == null ? 0 : jumlahKeranjang);
                        AppState.get().setADJDSet(completed);
                    });
                });
                return;
            }
            AppState.get().setADJDSet(isADJDCompleted);
        });
    }

    private void onSearch(String string) {
        String stringLC = string.toLowerCase(Locale.ROOT);
        JsonArray filtered = new JsonArray();
        for (JsonElement e : paketPenjualImmutableState) {
            JsonObject produk = Json.obj(e);
            String namaProduk = Json.str(produk, "nama");
            String namaProdukLC = namaProduk.toLowerCase(Locale.ROOT);

            if (namaProdukLC.indexOf(stringLC) >= 0) {
                filtered.add(produk);
            }
        }
        paketPenjualState = filtered;
        pencarian = stringLC;
        renderBody();
    }

    private void onClose() {
        paketPenjualState = paketPenjualImmutableState;
        pencarian = "";
        renderBody();
    }

    private void render() {
        removeAllViews();
        pencarianView = null;
        body = null;

        Context ctx = getContext();
        if (!isReadyToShow) {
            FrameLayout center = new FrameLayout(ctx);
            center.addView(PenjualUi.spinner(ctx, 25), new FrameLayout.LayoutParams(Ui.dp(ctx, 25), Ui.dp(ctx, 25),
                    Gravity.CENTER));
            addView(center, new LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        pencarianView = new Pencarian(ctx, string -> onSearch(string), () -> onClose(), "Cari Produk di Sini",
                pencarian);
        LinearLayout.LayoutParams pLp = PenjualUi.lp(ctx, Ui.MATCH, Ui.WRAP, 0, 5, 0, 0);
        addView(pencarianView, pLp);
        addView(Ui.spaceH(ctx, 5));

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

        if (paketPenjualState.size() >= 1) {
            List<View> cards = new ArrayList<>();
            for (JsonElement e : paketPenjualState) {
                final JsonObject produk = Json.obj(e);
                cards.add(new PaketPenjualCard(ctx, produk, () -> deteksiADJD(produk), () -> initialization()));
            }
            ScrollView scroll = PenjualUi.innerScroll(ctx);
            scroll.addView(Layouts.grid(ctx, cards, 3, 0f, 0f, 0.5f), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            body.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        } else {
            LinearLayout col = PenjualUi.emptyState(ctx, 25f, 5f, "Tidak Ada Produk", 3.5f,
                    CustomColors.textMuted);
            col.setGravity(Gravity.CENTER);
            body.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH, Gravity.CENTER));
        }
    }
}
