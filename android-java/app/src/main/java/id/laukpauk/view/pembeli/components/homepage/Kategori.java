package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.KategoriProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.ListProdukActivity;

/** Port of lib/views/pembeli/components/HomePage/Kategori.dart. */
public class Kategori extends LinearLayout {
    private String isThisKategoriFetchingProduct = ""; // id kategori
    private JsonArray kategoriProduk; // null while loading
    private final FrameLayout content;

    public Kategori(Context ctx) {
        super(ctx);
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        LinearLayout titleRow = Ui.row(ctx);
        titleRow.setPadding(0, 0, 0, Ui.dp(ctx, 5));
        titleRow.addView(HomeUi.sectionTitle(ctx, "Kategori Produk"));
        titleRow.addView(Ui.spacer(ctx));
        TextView lihat = Ui.text(ctx, "Lihat semua", hb * 3.0f, CustomColors.textMuted);
        Ui.onTap(lihat, () -> Nav.push(ctx, id.laukpauk.view.pembeli.components.homepage.SemuaKategoriProdukActivity.class));
        titleRow.addView(lihat);
        addView(titleRow, new LayoutParams(Ui.MATCH, Ui.WRAP));

        content = new FrameLayout(ctx);
        addView(content, new LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(Loading.view(ctx));

        HomeUi.act(ctx).async(() -> KategoriProdukModel.getKategoriProduk(7), result -> {
            kategoriProduk = result;
            render();
        });
    }

    private void render() {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        content.removeAllViews();
        if (kategoriProduk != null && kategoriProduk.size() >= 1) {
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(HORIZONTAL);
            for (int i = 0; i < kategoriProduk.size(); i++) {
                JsonObject kategori = Json.objAt(kategoriProduk, i);
                row.addView(item(kategori), new LayoutParams(Ui.WRAP, Ui.dp(ctx, hb * 25)));
            }
            HorizontalScrollView scroll = Layouts.hscroll(ctx, row);
            content.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, hb * 25)));
            return;
        }
        LinearLayout empty = HomeUi.emptyBlock(ctx, hb * 25, "Kategori Produk Tidak Ada", hb * 3.5f);
        content.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private View item(final JsonObject kategori) {
        final Context ctx = getContext();
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);

        final String idKategoriProduk = Json.str(kategori, "id");
        final String namaKategoriProduk = Json.str(kategori, "nama");
        String gambar = Json.str(kategori, "gambar");
        boolean isDefaultImage = gambar.equals(DefaultImage.defaultKategoriProduk);

        LinearLayout wrapper = new LinearLayout(ctx);
        wrapper.setOrientation(HORIZONTAL);

        FrameLayout stack = new FrameLayout(ctx);
        FrameLayout imageBox = new FrameLayout(ctx);
        Ui.background(imageBox, CustomColors.nearlyWhite, 10);
        Ui.clipRounded(imageBox, 10);
        imageBox.addView(HomeUi.netImage(ctx,
                ServerPath.baseURL + "/" + ServerPath.uploadGambarKategoriProduk + "/compress/" + gambar,
                isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP));
        stack.addView(imageBox, new FrameLayout.LayoutParams(Ui.dp(ctx, hb * 32.5f), Ui.dp(ctx, hb * 21.5f)));

        TextView nama = Ui.text(ctx, namaKategoriProduk, hb * 2.5f, 0xDD000000);
        nama.setSingleLine(true);
        nama.setEllipsize(android.text.TextUtils.TruncateAt.END);
        nama.setGravity(Gravity.CENTER);
        Ui.padXY(nama, 10, 5);
        Ui.background(nama, CustomColors.nearlyWhite, 50);
        FrameLayout.LayoutParams namaLp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        namaLp.setMargins(Ui.dp(ctx, 15), 0, Ui.dp(ctx, 15), Ui.dp(ctx, 5));
        // keep the label inside the card (SingleChildScrollView in Dart)
        nama.setMaxWidth(Ui.dp(ctx, hb * 32.5f - 30));
        stack.addView(nama, namaLp);

        if (isThisKategoriFetchingProduct.equals(idKategoriProduk)) {
            FrameLayout overlay = new FrameLayout(ctx);
            overlay.setBackgroundColor(0xAAFFFFFF);
            overlay.addView(Loading.view(ctx, null, null, 25f, 25f));
            stack.addView(overlay, new FrameLayout.LayoutParams(Ui.dp(ctx, hb * 32.5f), Ui.dp(ctx, hb * 21.5f)));
        }

        Ui.onTap(stack, () -> {
            isThisKategoriFetchingProduct = idKategoriProduk;
            render();

            act.async(() -> KategoriProdukModel.getProdukInThisCategory(idKategoriProduk,
                    "id, idPenjual, namaToko, foto, nama, namaKategoriProduk"), productInThisCategory -> {
                String title = "Daftar Produk";
                String subTitle = "Berikut ini adalah produk di dalam kategori \"" + namaKategoriProduk.toUpperCase()
                        + "\"";

                isThisKategoriFetchingProduct = "";
                render();

                // onRefresh: () async {} -> not passable; ListProduk never calls it
                Nav.push(act, ListProdukActivity.class, Json.of(
                        "title", title,
                        "subTitle", subTitle,
                        "isReadyToShow", true,
                        "dataProduk", productInThisCategory));
            });
        });

        wrapper.addView(stack, new LayoutParams(Ui.WRAP, Ui.MATCH));
        wrapper.addView(Ui.spaceW(ctx, 5));
        return wrapper;
    }
}
