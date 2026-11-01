package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.ProdukDiDalamPasarActivity;

/** Port of lib/views/pembeli/components/HomePage/Pasar.dart. */
public class Pasar extends FrameLayout {
    public Pasar(final Context ctx, final JsonObject dataPasar, boolean showDistance) {
        super(ctx);
        String distanceString = Json.strOrNull(dataPasar, "distance");
        Double distance = (distanceString != null) ? parse(distanceString) : null;

        // ClipRRect(12.5) > Container(border nearlyWhite) > Material > InkWell > Container(image)
        Ui.background(this, CustomColors.nearlyWhite, 5);
        Ui.clipRounded(this, 12.5f);

        addView(HomeUi.netImage(ctx, ServerPath.baseURL + "/" + ServerPath.uploadGambarPasar + "/compress/"
                + Json.str(dataPasar, "gambar"), ImageView.ScaleType.CENTER_CROP));

        android.view.View overlay = new android.view.View(ctx);
        overlay.setBackgroundColor(android.graphics.Color.argb(77, 0, 0, 0)); // rgba(0,0,0,0.3)
        addView(overlay, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        LinearLayout column = Ui.column(ctx);
        column.setGravity(Gravity.CENTER);
        Ui.padAll(column, 5);
        TextView nama = Ui.text(ctx, Json.str(dataPasar, "nama"), 14f, CustomColors.white, true);
        nama.setGravity(Gravity.CENTER);
        column.addView(nama, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        if (showDistance && distance != null) {
            column.addView(Ui.spaceH(ctx, 7));
            TextView jarak = Ui.text(ctx, HomeUi.fixed1(distance) + " KM", 13f, CustomColors.white, true);
            jarak.setGravity(Gravity.CENTER);
            column.addView(jarak, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }
        addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        Ui.onTap(this, () -> Nav.push(ctx, ProdukDiDalamPasarActivity.class, Json.of(
                "idPasar", Json.str(dataPasar, "id"),
                "namaPasar", Json.str(dataPasar, "nama"))));
    }

    private static Double parse(String s) {
        try {
            return Double.valueOf(Double.parseDouble(s.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
