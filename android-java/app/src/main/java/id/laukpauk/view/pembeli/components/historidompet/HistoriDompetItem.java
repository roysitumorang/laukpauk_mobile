package id.laukpauk.view.pembeli.components.historidompet;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/components/HistoriDompet/HistoriDompetItem.dart. */
public class HistoriDompetItem extends LinearLayout {

    public HistoriDompetItem(Context ctx, int nomor, JsonObject dataRiwayatDompet) {
        super(ctx);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.TOP);
        float hb = Ui.hb(ctx);
        // margin: only(bottom: 15), padding: symmetric(horizontal: 10)
        Ui.pad(this, 10, 0, 10, 15);

        long nilaiDompet = Json.asLong(Json.get(dataRiwayatDompet, "nilai"), 0);

        addView(Ui.text(ctx, nomor + ".", 14f, 0xDD000000, true), new LayoutParams(Ui.WRAP, Ui.WRAP));
        addView(Ui.spaceW(ctx, 15));

        LinearLayout column = Ui.column(ctx);
        String jenis = Json.str(dataRiwayatDompet, "jenis");
        if (jenis.length() >= 1) {
            column.addView(Ui.text(ctx, jenis.toUpperCase(Locale.ROOT), hb * 4f, 0xDD000000, true));
        } else {
            TextView t = Ui.text(ctx, "Tanpa Jenis Dompet",
                    new Txt.Style(hb * 4f).color(CustomColors.textMuted).italic().bold());
            column.addView(t);
        }
        column.addView(Ui.spaceH(ctx, 5));
        String keterangan = Json.str(dataRiwayatDompet, "keterangan");
        if (keterangan.length() >= 1) {
            column.addView(Ui.text(ctx, keterangan, hb * 3.25f, 0xDD000000));
        } else {
            column.addView(Ui.text(ctx, "Tanpa Keterangan",
                    new Txt.Style(hb * 3.25f).color(CustomColors.textMuted).italic().bold()));
        }
        column.addView(Ui.spaceH(ctx, 5));
        column.addView(Ui.text(ctx, "Pada " + Json.str(dataRiwayatDompet, "createdAt"), hb * 3.25f, 0xDD000000));
        addView(column, Ui.weightW(1f));

        addView(Ui.text(ctx, Currency.formatRupiah(nilaiDompet, 0), hb * 4f,
                nilaiDompet >= 0 ? CustomColors.green : CustomColors.textDanger, true),
                new LayoutParams(Ui.WRAP, Ui.WRAP));
    }
}
