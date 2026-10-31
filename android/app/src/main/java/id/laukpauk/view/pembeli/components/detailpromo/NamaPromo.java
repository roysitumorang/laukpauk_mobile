package id.laukpauk.view.pembeli.components.detailpromo;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.Currency;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/DetailPromo/NamaPromo.dart. */
public class NamaPromo extends LinearLayout {

    /** Dart: NamaPromo({required detailPromo, required detailSatuan, required detailPromoItem}). */
    public NamaPromo(Context ctx, JsonObject detailPromo, JsonObject detailSatuan,
                     JsonObject detailPromoItem) {
        super(ctx);
        setOrientation(VERTICAL);
        float hb = Ui.hb(ctx);

        String namaPromo = Json.str(detailPromo, "nama");
        String statusPromo = Json.str(detailPromo, "status");
        String statusPromoLC = statusPromo.toLowerCase(Locale.ROOT);
        String deskripsi = Json.str(detailPromo, "deskripsi");

        String namaSatuan = Json.str(detailSatuan, "nama");
        String hargaSatuan = Json.str(detailSatuan, "harga");
        String diskonSatuan = Json.str(detailSatuan, "diskon");

        int diskonPromo = Json.parseInt(Json.str(detailPromoItem, "diskon"), 0);
        int hargaInt = Json.parseInt(hargaSatuan, 0);
        int diskonInt = Json.parseInt(diskonSatuan, 0);

        int hargaBersih = hargaInt - (diskonInt + diskonPromo);

        boolean isPromoAktif = statusPromoLC.equals("aktif");

        Ui.padXY(this, 15, 10);
        setBackground(Ui.shape(ctx, isPromoAktif ? 0x66FFC107 : 0x66DC3545, 10));

        LinearLayout top = new LinearLayout(ctx);
        top.setOrientation(HORIZONTAL);
        TextView nama = Ui.text(ctx, namaPromo, hb * 3.25f, 0xDD000000);
        nama.setTypeface(nama.getTypeface(), Typeface.BOLD);
        top.addView(nama, new LayoutParams(0, Ui.WRAP, 1f));

        LinearLayout right = Ui.column(ctx);
        right.setGravity(Gravity.END);
        TextView harga = Ui.text(ctx, Currency.formatRupiah(hargaBersih, 0), hb * 3.75f, 0xDD000000);
        harga.setTypeface(harga.getTypeface(), Typeface.BOLD);
        right.addView(harga);
        right.addView(Ui.spaceH(ctx, 3.5f));
        right.addView(Ui.text(ctx, "Khusus " + namaSatuan, hb * 2.75f, 0xDD000000));
        top.addView(right, new LayoutParams(0, Ui.WRAP, 1f));
        addView(top, new LayoutParams(Ui.MATCH, Ui.WRAP));

        android.view.View line = Ui.divider(ctx, 0xFF000000, 0.45f);
        LayoutParams lineLp = new LayoutParams(Ui.MATCH, Math.max(1, Ui.dp(ctx, 0.45f)));
        lineLp.topMargin = Ui.dp(ctx, 10);
        lineLp.bottomMargin = Ui.dp(ctx, 10);
        addView(line, lineLp);

        addView(Ui.text(ctx, "Keterangan Promo", 14f, 0xDD000000, true));
        addView(Ui.spaceH(ctx, 5));
        addView(Ui.text(ctx, deskripsi, hb * 2.75f, 0xDD000000));
        if (!isPromoAktif) {
            TextView tidak = Ui.text(ctx, "Promo tidak tersedia untuk saat ini!", hb * 3.5f, 0xDD000000);
            LayoutParams lp = new LayoutParams(Ui.WRAP, Ui.WRAP);
            lp.topMargin = Ui.dp(ctx, 15);
            addView(tidak, lp);
        }
    }
}
