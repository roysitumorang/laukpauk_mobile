package id.laukpauk.view.pembeli.components.detailpromo;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.PenjualActivity;

/** Port of lib/views/pembeli/components/DetailPromo/NamaPenjual.dart. */
public class NamaPenjual extends LinearLayout {

    public NamaPenjual(Context ctx, JsonObject detailPenjual) {
        super(ctx);
        setOrientation(HORIZONTAL);
        final Activity activity = (Activity) ctx;
        float hb = Ui.hb(ctx);

        String namaToko = Json.str(detailPenjual, "namaToko");
        final String idPenjual = Json.str(detailPenjual, "idToko");

        addView(Ui.asset(ctx, "store.png", hb * 7.5f));
        addView(Ui.spaceW(ctx, 10));

        LinearLayout column = Ui.column(ctx);

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(Ui.text(ctx, "Penjual", hb * 3.5f, CustomColors.textMuted));
        row.addView(Ui.spaceW(ctx, 5));
        TextView nama = Ui.text(ctx, namaToko.length() >= 1 ? namaToko : "Penjual Tidak Diketahui",
                hb * 3.5f, CustomColors.textInfo);
        nama.setTypeface(nama.getTypeface(), android.graphics.Typeface.BOLD);
        nama.setOnClickListener(v -> Nav.push(activity, PenjualActivity.class,
                Json.of("id", idPenjual)));
        row.addView(nama);
        HorizontalScrollView scroll = Layouts.hscroll(ctx, row);
        column.addView(scroll, new LayoutParams(Ui.MATCH, Ui.WRAP));

        column.addView(Ui.text(ctx, "Klik nama penjual untuk melihat seluruh produk penjual",
                hb * 2.5f, 0xDD000000));

        LayoutParams colLp = new LayoutParams(0, Ui.WRAP, 1f);
        addView(column, colLp);
        setGravity(Gravity.CENTER_VERTICAL);
    }
}
