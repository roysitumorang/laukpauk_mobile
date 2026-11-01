package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;

import id.laukpauk.core.Json;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/HomePage/ProdukPasarSekitar.dart (class ProdukPasarSekitar). */
public class ProdukPasarSekitar extends LinearLayout {
    public ProdukPasarSekitar(Context ctx, JsonArray pasarSekitarSaya) {
        super(ctx);
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        // length >= 3 -> only the first two markets
        int count = pasarSekitarSaya.size() >= 3 ? 2 : pasarSekitarSaya.size();

        addView(Ui.text(ctx, "Pasar Terdekat", hb * 4.0f, 0xDD000000), new LayoutParams(Ui.WRAP, Ui.WRAP));
        addView(Ui.spaceH(ctx, 10));
        for (int i = 0; i < count; i++) {
            LayoutParams lp = new LayoutParams(Ui.MATCH, Ui.WRAP);
            lp.bottomMargin = Ui.dp(ctx, 15); // Container(margin: EdgeInsets.only(bottom: 15))
            addView(new ProdukPasar(ctx, Json.objAt(pasarSekitarSaya, i)), lp);
        }
        addView(Ui.spaceH(ctx, 15));
    }
}
