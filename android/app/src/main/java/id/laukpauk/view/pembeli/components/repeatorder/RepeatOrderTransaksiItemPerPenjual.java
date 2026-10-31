package id.laukpauk.view.pembeli.components.repeatorder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.detailtransaksi.TransaksiUi;

/** Port of lib/views/pembeli/components/RepeatOrder/RepeatOrderTransaksiItemPerPenjual.dart. */
public class RepeatOrderTransaksiItemPerPenjual extends LinearLayout {

    /** Dart: (isSelected, isAvailable, idTransaksi, detailPenjual, itemTransaksi, toggleSelected). */
    public RepeatOrderTransaksiItemPerPenjual(Context ctx, boolean isSelected, boolean isAvailable,
                                              String idTransaksi, JsonObject detailPenjual, JsonArray itemTransaksi,
                                              final Async.Done<Boolean> toggleSelected) {
        super(ctx);
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        String namaPenjual = Json.str(detailPenjual, "namaPenjual");
        String alamatToko = Json.str(detailPenjual, "alamatToko");
        if (alamatToko.length() <= 0) {
            alamatToko = "Alamat toko belum ditentukan";
        }

        String namaPasar = Json.str(detailPenjual, "namaPasar");
        String bannerPenjual = Json.str(detailPenjual, "bannerPenjual");

        String bannerPenjualURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarBannerPenjual
                + "/compress/" + bannerPenjual;

        float imgWidthHeight = hb * 10.5f;

        // Card(elevation 5, side: BorderSide(color), radius 10) with the default 4dp margin
        LinearLayout inner = Ui.column(ctx);
        inner.setBackground(Ui.shape(ctx, CustomColors.white, 10f,
                !isAvailable ? CustomColors.textDanger : CustomColors.nearlyWhite, 1f));
        inner.setElevation(Ui.dp(ctx, 5));
        Ui.pad(inner, 15, 15, 15, 15);
        LayoutParams lp = new LayoutParams(Ui.MATCH, Ui.WRAP);
        int m = Ui.dp(ctx, 4);
        lp.setMargins(m, m, m, m);

        LinearLayout row = Ui.row(ctx);
        row.setGravity(Gravity.CENTER_VERTICAL);
        if (isAvailable) {
            CheckBox cb = new CheckBox(ctx);
            cb.setChecked(isSelected);
            cb.setButtonTintList(ColorStateList.valueOf(CustomColors.green));
            cb.setOnCheckedChangeListener((button, checked) -> {
                if (toggleSelected != null) {
                    toggleSelected.run(checked);
                }
            });
            row.addView(cb, new LayoutParams(Ui.WRAP, Ui.WRAP));
            row.addView(Ui.spaceW(ctx, 5f));
        }
        ImageView img = Ui.network(ctx, bannerPenjualURL, imgWidthHeight, imgWidthHeight);
        Ui.clipRounded(img, imgWidthHeight / 2f);
        row.addView(img, new LayoutParams(Ui.dp(ctx, imgWidthHeight), Ui.dp(ctx, imgWidthHeight)));
        row.addView(Ui.spaceW(ctx, 10f));

        LinearLayout info = Ui.column(ctx);
        info.addView(TransaksiUi.hbText(ctx, namaPenjual, 3.0f, TransaksiUi.BLACK87, false));
        info.addView(Ui.spaceH(ctx, 1.5f));
        info.addView(TransaksiUi.hbText(ctx, alamatToko, 2.5f, CustomColors.textMuted, false));
        info.addView(Ui.spaceH(ctx, 5.0f));
        info.addView(TransaksiUi.hbText(ctx, "Berjualan di " + namaPasar, 2.5f, CustomColors.textMuted, false));
        row.addView(info, new LayoutParams(0, Ui.WRAP, 1f));
        inner.addView(row, new LayoutParams(Ui.MATCH, Ui.WRAP));

        if (isAvailable) {
            TextView label = TransaksiUi.hbText(ctx, "Item Belanjaan", 3.25f, TransaksiUi.BLACK87, false);
            Ui.padXY(label, 7.5f, 0);
            LayoutParams llp = new LayoutParams(Ui.MATCH, Ui.WRAP);
            llp.topMargin = Ui.dp(ctx, 10);
            llp.bottomMargin = Ui.dp(ctx, 10);
            inner.addView(label, llp);

            LinearLayout items = Ui.column(ctx);
            Ui.padXY(items, 7.5f, 0);
            items.addView(new ItemTransaksiRO(ctx, itemTransaksi), new LayoutParams(Ui.MATCH, Ui.WRAP));
            inner.addView(items, new LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            inner.addView(Ui.spaceH(ctx, 15f));
            LinearLayout col = Ui.column(ctx);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView t1 = TransaksiUi.hbText(ctx, "Tidak Dapat Dibeli Ulang", 3.25f, CustomColors.textDanger, false);
            t1.setGravity(Gravity.CENTER);
            col.addView(t1, new LayoutParams(Ui.MATCH, Ui.WRAP));
            col.addView(Ui.spaceH(ctx, 5.0f));
            TextView t2 = TransaksiUi.hbText(ctx,
                    "Mohon maaf, belanjaan tidak dapat direpeat order karena penjual tidak ready pada alamat dan jam pengantaran yang telah anda tentukan",
                    2.75f, CustomColors.textMuted, false);
            t2.setGravity(Gravity.CENTER);
            col.addView(t2, new LayoutParams(Ui.MATCH, Ui.WRAP));
            inner.addView(col, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        addView(inner, lp);
    }
}
