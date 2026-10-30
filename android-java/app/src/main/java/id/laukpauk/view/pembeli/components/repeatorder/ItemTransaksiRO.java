package id.laukpauk.view.pembeli.components.repeatorder;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.detailtransaksi.TransaksiUi;

/** Port of lib/views/pembeli/components/RepeatOrder/ItemTransaksiRO.dart. */
public class ItemTransaksiRO extends LinearLayout {

    public ItemTransaksiRO(Context ctx, JsonArray itemTransaksi) {
        super(ctx);
        setOrientation(VERTICAL);
        Ui.pad(this, 0, 0, 0, 5);

        if (itemTransaksi.size() >= 1) {
            LinearLayout column = Ui.column(ctx);
            Ui.pad(column, 0, 0, 0, 7);
            for (int i = 0; i < itemTransaksi.size(); i++) {
                column.addView(itemRow(ctx, Json.objAt(itemTransaksi, i)),
                        new LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            addView(column, new LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            LinearLayout empty = Ui.column(ctx);
            empty.setGravity(Gravity.CENTER_HORIZONTAL);
            empty.addView(Ui.asset(ctx, "empty.png", Ui.hb(ctx) * 20));
            empty.addView(Ui.spaceH(ctx, 5));
            empty.addView(TransaksiUi.hbText(ctx, "Transaksi ini tidak memiliki item belanjaan", 3f,
                    CustomColors.textMuted, false));
            empty.addView(Ui.spaceH(ctx, 10));
            addView(empty, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }

    private static LinearLayout itemRow(Context ctx, JsonObject item) {
        int harga = Json.parseInt(Json.str(item, "harga"), 0);
        int diskon = Json.parseInt(Json.str(item, "diskon"), 0);
        int quantity = Json.parseInt(Json.str(item, "quantity"), 0);
        int totalHarga = quantity * (harga - diskon);

        String jenisProduk = Json.str(item, "jenisProduk");
        String namaSatuan;
        if (jenisProduk.toLowerCase().equals("nonpaket")) {
            namaSatuan = Json.str(item, "namaSatuan");
        } else {
            namaSatuan = "paket";
        }
        String catatan = Json.str(item, "catatan");

        LinearLayout col = Ui.column(ctx);
        Ui.pad(col, 0, 0, 0, 10);

        LinearLayout row = Ui.row(ctx);
        row.addView(TransaksiUi.content(ctx, Json.str(item, "namaProduk") + " " + quantity + " x " + namaSatuan,
                CustomColors.nearlyBlack), new LayoutParams(0, Ui.WRAP, 1f));
        row.addView(Ui.spaceW(ctx, 5));
        TextView total = TransaksiUi.title(ctx, Currency.formatRupiah(totalHarga, 0), CustomColors.green);
        total.setGravity(Gravity.END);
        row.addView(total, new LayoutParams(Ui.WRAP, Ui.WRAP));
        col.addView(row, new LayoutParams(Ui.MATCH, Ui.WRAP));

        col.addView(Ui.spaceH(ctx, 3.5f));
        if (catatan.length() >= 1) {
            col.addView(Ui.spaceH(ctx, 3));
            TextView c = TransaksiUi.catatan(ctx, "Catatan : " + catatan, CustomColors.nearlyBlack);
            c.setBackground(Ui.shape(ctx, 0x5566CC99, 10f));
            Ui.padXY(c, 10, 5);
            col.addView(c, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        return col;
    }
}
