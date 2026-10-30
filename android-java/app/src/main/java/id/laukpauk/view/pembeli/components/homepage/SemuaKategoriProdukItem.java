package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.KategoriProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.ListProdukActivity;

/** Port of lib/views/pembeli/components/HomePage/SemuaKategoriProdukItem.dart. */
public class SemuaKategoriProdukItem extends FrameLayout {
    public SemuaKategoriProdukItem(final Context ctx, JsonObject dataKategori) {
        super(ctx);
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);

        final String idKategoriProduk = Json.str(dataKategori, "id");
        final String nama = Json.str(dataKategori, "nama");
        String gambar = Json.str(dataKategori, "gambar");

        FrameLayout image = new FrameLayout(ctx);
        Ui.background(image, CustomColors.nearlyWhite, 10);
        Ui.clipRounded(image, 10);
        image.addView(HomeUi.netImage(ctx, ServerPath.baseURL + "/" + ServerPath.uploadGambarKategoriProduk
                + "/compress/" + gambar, ImageView.ScaleType.CENTER_CROP));
        addView(image, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        View overlay = new View(ctx);
        overlay.setBackground(Ui.shape(ctx, android.graphics.Color.argb(102, 0, 0, 0), 10)); // rgba(0,0,0,0.4)
        addView(overlay, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        TextView label = Ui.text(ctx, nama, hb * 3.0f, CustomColors.nearlyWhite);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP, Gravity.BOTTOM);
        lp.setMargins(Ui.dp(ctx, 7.5f), 0, Ui.dp(ctx, 7.5f), Ui.dp(ctx, 7.5f));
        addView(label, lp);

        Ui.onTap(this, () -> act.async(() -> KategoriProdukModel.getProdukInThisCategory(idKategoriProduk),
                productInThisCategory -> {
                    String title = "Daftar Produk";
                    String subTitle = "Berikut ini adalah produk di dalam kategori \"" + nama.toUpperCase() + "\"";
                    // onRefresh (re-fetch the category) is never invoked by ListProduk -> not passed
                    Nav.push(act, ListProdukActivity.class, Json.of(
                            "title", title,
                            "subTitle", subTitle,
                            "isReadyToShow", true,
                            "dataProduk", productInThisCategory));
                }));
    }
}
