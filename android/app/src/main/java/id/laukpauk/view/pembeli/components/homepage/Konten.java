package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.KontenModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.ListProdukActivity;

/** Port of lib/views/pembeli/components/HomePage/Konten.dart. */
public class Konten extends LinearLayout {
    public Konten(final Context ctx, JsonObject detailKonten) {
        super(ctx);
        setOrientation(VERTICAL);
        setPadding(0, 0, 0, Ui.dp(ctx, 15));
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);

        final String idKonten = Json.str(detailKonten, "id");
        final String namaDivisi = Json.str(detailKonten, "namaDivisi");
        String jenisItem = Json.str(detailKonten, "jenisItem");
        String jenisItemLC = jenisItem.toLowerCase();

        LinearLayout row = Ui.row(ctx);
        row.addView(HomeUi.sectionTitle(ctx, namaDivisi));
        row.addView(Ui.spacer(ctx));
        if (jenisItemLC.equals("produk")) {
            TextView lihat = Ui.text(ctx, "Lihat Semua", hb * 3.0f, CustomColors.darkGreen);
            Ui.onTap(lihat, () -> act.async(() -> KontenModel.getProdukKonten(idKonten), getProdukKontenItem -> {
                String title = namaDivisi.toUpperCase();
                String subTitle = "Berikut ini adalah produk - produk " + namaDivisi.toUpperCase();
                Nav.push(act, ListProdukActivity.class, Json.of(
                        "title", title,
                        "subTitle", subTitle,
                        "dataProduk", getProdukKontenItem,
                        "isReadyToShow", true));
            }));
            row.addView(lihat);
        }
        addView(row, new LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));
        addView(new KontenItem(ctx, detailKonten), new LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
