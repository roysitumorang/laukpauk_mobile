package id.laukpauk.ui;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.pembeli.DetailProdukActivity;

/**
 * lib/global-components/SearchResult.dart: the suggestion list for a search term. Like the Dart
 * widget it shows the "Yuk, cari produk ..." prompt until the search string CHANGES
 * ({@link #setSearchString(String)} = didUpdateWidget).
 */
public class SearchResult extends FrameLayout {
    private String searchString;
    private String searchStringState;
    private int generation = 0;
    private final Runnable onOpen;

    public SearchResult(Context ctx, String searchString) {
        this(ctx, searchString, null);
    }

    /** @param onOpen runs right before a suggestion's screen is opened (HeaderSearch closes its dialog here). */
    public SearchResult(Context ctx, String searchString, Runnable onOpen) {
        super(ctx);
        this.searchString = searchString;
        this.onOpen = onOpen;
        render();
    }

    /** didUpdateWidget: only a different string starts a new search. */
    public void setSearchString(String newString) {
        String old = searchString;
        searchString = newString;
        if (old == null ? newString != null : !old.equals(newString)) {
            searchStringState = newString;
            render();
        }
    }

    private JsonArray getSuggestions(String term) {
        Async.sleep(500);

        String params = HTTPRequest.getQueryString("term=" + Http.enc(term));
        Http.Response httpResponse = Http.getApi("/Konten/pencarian?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "hasilPencarian");
        }
        return new JsonArray();
    }

    private void render() {
        final Context ctx = getContext();
        removeAllViews();
        final float hb = Ui.hb(ctx);

        if (searchStringState == null) {
            LinearLayout column = Ui.column(ctx);
            column.setGravity(Gravity.CENTER_HORIZONTAL);
            ImageView img = Ui.asset(ctx, "produk.png", hb * 30f);
            img.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 30f), Ui.WRAP));
            column.addView(img);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            tlp.topMargin = Ui.dp(ctx, 10);
            column.addView(Ui.text(ctx, "Yuk, cari produk yang ingin kamu beli disini .. ", hb * 3.25f,
                    CustomColors.textMuted), tlp);
            addView(column, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
            return;
        }

        addView(Loading.view(ctx), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        final int myGeneration = ++generation;
        final String term = searchStringState;
        Async.run(() -> getSuggestions(term), (JsonArray hasilPencarian) -> {
            if (myGeneration != generation) {
                return;
            }
            removeAllViews();
            if (hasilPencarian.size() >= 1) {
                addView(buildList(ctx, hasilPencarian, term), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
                return;
            }
            LinearLayout column = Ui.column(ctx);
            column.setGravity(Gravity.CENTER_HORIZONTAL);
            ImageView img = Ui.asset(ctx, "empty.png", hb * 30f);
            img.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 30f), Ui.WRAP));
            column.addView(img);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            tlp.topMargin = Ui.dp(ctx, 10);
            TextView t = Ui.text(ctx, "Produk \"" + term + "\" tidak ditemukan. Coba cari lagi ya ..",
                    hb * 3.25f, CustomColors.textMuted);
            t.setGravity(Gravity.CENTER);
            column.addView(t, tlp);
            addView(column, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.CENTER));
        });
    }

    private View buildList(final Context ctx, JsonArray hasilPencarian, final String term) {
        final float hb = Ui.hb(ctx);
        LinearLayout list = Ui.column(ctx);

        for (int i = 0; i < hasilPencarian.size(); i++) {
            JsonObject suggestion = Json.objAt(hasilPencarian, i);
            final String jenisItem = Json.str(suggestion, "jenisItem").toLowerCase();
            final boolean penjual = jenisItem.equals("penjual");

            String leading = penjual ? Json.strOrNull(suggestion, "bannerToko") : Json.strOrNull(suggestion, "fotoProduk");
            String title = penjual ? Json.strOrNull(suggestion, "namaToko") : Json.strOrNull(suggestion, "namaProduk");
            String subtitle = penjual ? Json.strOrNull(suggestion, "alamatToko") : Json.strOrNull(suggestion, "namaPenjual");
            final String id = Json.str(suggestion, "id");

            if (subtitle == null || subtitle.length() <= 0) {
                subtitle = penjual ? "Alamat Toko Belum ditentukan" : "Nama Penjual Belum ditentukan";
            }
            if (title == null || title.length() <= 0) {
                title = penjual ? "Nama Toko / Penjual belum ditentukan" : "Nama Produk Belum ditentukan";
            }

            boolean isLeadingNull = leading == null || leading.length() <= 0;

            ImageView iv = new ImageView(ctx);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            if (isLeadingNull) {
                Ui.setAsset(iv, "laukpauk.png");
            } else {
                String imagePath = penjual
                        ? ServerPath.uploadGambarBannerPenjual + "/compress/" + leading
                        : ServerPath.uploadGambarProduk + "/compress/" + leading;
                Ui.loadNetworkCircle(iv, ServerPath.baseURL + "/" + imagePath);
            }

            LinearLayout tile = Ui.row(ctx);
            tile.setGravity(Gravity.CENTER_VERTICAL);
            Ui.pad(tile, 16, 8, 16, 8);
            tile.addView(iv, new LinearLayout.LayoutParams(Ui.dp(ctx, 50), Ui.dp(ctx, 50)));
            if (isLeadingNull) {
                Ui.clipRounded(iv, 25f);
            }

            LinearLayout texts = Ui.column(ctx);
            LinearLayout titleRow = Ui.row(ctx);
            titleRow.setGravity(Gravity.CENTER_VERTICAL);
            TextView badge = Ui.text(ctx, jenisItem.toUpperCase(), hb * 2.5f, CustomColors.nearlyWhite);
            Ui.pad(badge, 10, 3, 10, 3);
            badge.setBackground(Ui.shape(ctx, penjual ? CustomColors.textPrimary : CustomColors.green, 7.5f));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            blp.rightMargin = Ui.dp(ctx, 7.5f);
            titleRow.addView(badge, blp);
            titleRow.addView(Ui.text(ctx, title, 16f, 0xDE000000));
            HorizontalScrollView titleScroll = new HorizontalScrollView(ctx);
            titleScroll.setHorizontalScrollBarEnabled(false);
            titleScroll.addView(titleRow, new HorizontalScrollView.LayoutParams(Ui.WRAP, Ui.WRAP));
            texts.addView(titleScroll, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            texts.addView(Ui.text(ctx, subtitle, 14f, 0x99000000));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0, Ui.WRAP, 1f);
            tlp.leftMargin = Ui.dp(ctx, 16);
            tile.addView(texts, tlp);

            Layouts.ripple(tile, 0x22000000, 0f);
            tile.setOnClickListener(v -> {
                if (onOpen != null) {
                    onOpen.run();
                }
                if (penjual) {
                    Nav.push(ctx, PenjualActivity.class, Json.of("id", id));
                } else {
                    Nav.push(ctx, DetailProdukActivity.class, Json.of("id", id));
                }
            });
            list.addView(tile, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        ScrollView scroll = new ScrollView(ctx);
        scroll.addView(list, new ScrollView.LayoutParams(Ui.MATCH, Ui.WRAP));
        final SwipeRefreshLayout refresh = new SwipeRefreshLayout(ctx);
        refresh.setColorSchemeColors(CustomColors.green);
        refresh.addView(scroll, new ViewGroupLayoutParams());
        refresh.setOnRefreshListener(() -> Async.run(() -> getSuggestions(term), (JsonArray ignored) -> refresh.setRefreshing(false),
                error -> refresh.setRefreshing(false)));
        return refresh;
    }

    private static final class ViewGroupLayoutParams extends android.view.ViewGroup.LayoutParams {
        ViewGroupLayoutParams() {
            super(Ui.MATCH, Ui.MATCH);
        }
    }
}
