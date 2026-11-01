package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.PasarModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.ProdukDiDalamPasarActivity;
import id.laukpauk.view.pembeli.components.produkdidalampasar.ProdukInThisPasarItem;

/** Port of class ProdukPasar in lib/views/pembeli/components/HomePage/ProdukPasarSekitar.dart. */
public class ProdukPasar extends LinearLayout {
    public ProdukPasar(final Context ctx, JsonObject pasar) {
        super(ctx);
        setOrientation(VERTICAL);
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);

        final String idPasar = Json.str(pasar, "id");
        final String nama = Json.str(pasar, "nama");
        String gambar = Json.str(pasar, "gambar");
        String gambarPasarURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarPasar + "/compress/" + gambar;

        String jarakString = Json.strOrNull(pasar, "distance");
        Double jarak = null;
        if (jarakString != null) {
            try {
                jarak = Double.valueOf(Double.parseDouble(jarakString.trim()));
            } catch (NumberFormatException e) {
                jarak = null;
            }
        }

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView foto = new ImageView(ctx);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetworkCircle(foto, gambarPasarURL);
        row.addView(foto, new LinearLayout.LayoutParams(Ui.dp(ctx, 50), Ui.dp(ctx, 50)));
        row.addView(Ui.spaceW(ctx, 15));

        LinearLayout info = Ui.column(ctx);
        info.addView(Ui.text(ctx, "Produk Pasar " + nama, hb * 3.5f, 0xDD000000, true));
        if (jarak != null) {
            info.addView(Ui.spaceH(ctx, 2.5f));
            info.addView(HomeUi.t(ctx, HomeUi.fixed1(jarak) + " KM", hb * 2.75f));
        }
        row.addView(info, Ui.weightW(1f));
        row.addView(Ui.spaceW(ctx, 15));

        TextView lihat = Ui.text(ctx, "Lihat Semua", hb * 3.0f, CustomColors.darkGreen);
        Ui.onTap(lihat, () -> Nav.push(ctx, ProdukDiDalamPasarActivity.class,
                Json.of("idPasar", idPasar, "namaPasar", nama)));
        row.addView(lihat);
        addView(row, new LayoutParams(Ui.MATCH, Ui.WRAP));

        final LinearLayout produkHolder = Ui.column(ctx);
        produkHolder.setPadding(0, Ui.dp(ctx, 10), 0, 0);
        produkHolder.addView(Loading.view(ctx, null, null, 25f, 25f));
        addView(produkHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));

        final float ww = hb * 35;
        act.async(() -> PasarModel.getProdukInThisPasar(idPasar, "y", "0", null), (JsonArray produkPasar) -> {
            produkHolder.removeAllViews();
            LinearLayout list = new LinearLayout(ctx);
            list.setOrientation(HORIZONTAL);
            for (int i = 0; i < produkPasar.size(); i++) {
                final JsonObject dataProduk = Json.objAt(produkPasar, i);
                ProdukInThisPasarItem item = new ProdukInThisPasarItem(ctx, dataProduk,
                        () -> HomeUi.pilihAdjd(act, dataProduk), Double.valueOf(ww));
                list.addView(item, new LinearLayout.LayoutParams(Ui.WRAP, Ui.dp(ctx, 275)));
            }
            produkHolder.addView(Layouts.hscroll(ctx, list), new LayoutParams(Ui.MATCH, Ui.dp(ctx, 275)));
        });
    }
}
