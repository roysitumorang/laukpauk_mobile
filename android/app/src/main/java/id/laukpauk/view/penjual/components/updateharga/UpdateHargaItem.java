package id.laukpauk.view.penjual.components.updateharga;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.penjual.PenjualUi;

/** Port of lib/views/penjual/components/UpdateHarga/UpdateHargaItem.dart. */
public class UpdateHargaItem extends LinearLayout {
    public UpdateHargaItem(Context ctx, JsonObject dataProduk) {
        super(ctx);
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        LinearLayout card = PenjualUi.cardPadded(ctx, 15, 20);
        addView(card, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(HORIZONTAL);
        ImageView foto = new ImageView(ctx);
        Ui.loadNetworkCircle(foto, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/"
                + Json.str(dataProduk, "foto"));
        row.addView(foto, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 15), Ui.dp(ctx, hb * 15)));
        row.addView(Ui.spaceW(ctx, 15));

        LinearLayout col = Ui.column(ctx);
        col.addView(PenjualUi.text(ctx, Json.str(dataProduk, "nama").toUpperCase(Locale.ROOT), 4f, 0xDD000000, true));
        col.addView(Ui.spaceH(ctx, 3));
        col.addView(PenjualUi.text(ctx, Json.str(dataProduk, "namaKategoriProduk"), 3.5f, CustomColors.textMuted,
                false));
        row.addView(col, Ui.weightW(1f));
        card.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        card.addView(Ui.spaceH(ctx, 15));
        card.addView(new SatuanProduk(ctx, Json.str(dataProduk, "id")), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
