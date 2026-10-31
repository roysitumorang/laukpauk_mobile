package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Json;
import id.laukpauk.model.KontenModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.produkdidalampasar.ProdukInThisPasarItem;

/** Port of lib/views/pembeli/components/HomePage/KontenItem.dart. */
public class KontenItem extends FrameLayout {
    private final JsonObject detailKonten;

    public KontenItem(Context ctx, JsonObject detailKonten) {
        super(ctx);
        this.detailKonten = detailKonten;
        addView(Loading.view(ctx));
        final String idKonten = Json.asStr(Json.get(detailKonten, "id"));
        HomeUi.act(ctx).async(() -> KontenModel.getProdukKonten(idKonten, 8), this::show);
    }

    private void show(JsonArray kontenItem) {
        final Context ctx = getContext();
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);
        removeAllViews();
        String jenisItem = Json.asStr(Json.get(detailKonten, "jenisItem"));

        if (kontenItem != null && kontenItem.size() >= 1) {
            if (jenisItem.equals("produk")) {
                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                for (int i = 0; i < kontenItem.size(); i++) {
                    final JsonObject dataProduk = Json.objAt(kontenItem, i);
                    ProdukInThisPasarItem item = new ProdukInThisPasarItem(ctx, dataProduk,
                            () -> HomeUi.pilihAdjd(act, dataProduk), Double.valueOf(hb * 35));
                    row.addView(item, new LinearLayout.LayoutParams(Ui.WRAP, Ui.dp(ctx, 275)));
                }
                addView(Layouts.hscroll(ctx, row), new LayoutParams(Ui.MATCH, Ui.dp(ctx, 275)));
            } else {
                addView(new KontenItemBanner(ctx, detailKonten, kontenItem), new LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            return;
        }

        LinearLayout empty = HomeUi.emptyBlock(ctx, hb * 25, "Tidak Ada Item Konten !", hb * 3.5f);
        empty.setPadding(0, Ui.dp(ctx, 15), 0, Ui.dp(ctx, 15));
        addView(empty, new LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
