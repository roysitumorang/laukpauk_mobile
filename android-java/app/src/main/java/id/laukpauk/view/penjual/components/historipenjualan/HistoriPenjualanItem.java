package id.laukpauk.view.penjual.components.historipenjualan;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.HistoriPenjualanModel;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.detailtransaksi.ItemTransaksiActivity;
import id.laukpauk.view.penjual.PesananScreenUi;

/**
 * Port of lib/views/penjual/components/HistoriPenjualan/HistoriPenjualanItem.dart.
 * Dart: HistoriPenjualanItem(Map dataPenjualan).
 */
public class HistoriPenjualanItem extends LinearLayout {
    private final JsonObject dataPenjualan;

    public HistoriPenjualanItem(Context ctx, JsonObject dataPenjualan) {
        super(ctx);
        this.dataPenjualan = dataPenjualan == null ? new JsonObject() : dataPenjualan;
        render();
    }

    private void render() {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);

        String namaUser = Json.str(dataPenjualan, "namaUser");
        String tanggalCheckout = Json.str(dataPenjualan, "waktuCheckout");
        String nomorTransaksi = Json.str(dataPenjualan, "nomorTransaksi");
        String status = Json.str(dataPenjualan, "status");
        final String idTransaksi = Json.str(dataPenjualan, "id");

        Txt.Style teks = new Txt.Style(hb * 3f).color(CustomColors.grey);

        // InkWell(child: Container(margin: bottom 5, decoration: nearlyWhite + radius 10 + border 0.75 green))
        setOrientation(VERTICAL);
        setLayoutParams(PesananScreenUi.lp(ctx, Ui.MATCH, Ui.WRAP, 0, 0, 0, 5));
        setBackground(Ui.shape(ctx, CustomColors.nearlyWhite, 10f, CustomColors.green, 0.75f));
        Ui.pad(this, 15, 15, 15, 15);
        Layouts.ripple(this, 0x33000000, 10f);

        Ui.onTap(this, new Runnable() {
            @Override
            public void run() {
                PesananScreenUi.run(ctx, new Async.Work<Integer>() {
                    @Override
                    public Integer run() {
                        return LocalStorage.getIdPenjualFromActiveUser();
                    }
                }, new Async.Done<Integer>() {
                    @Override
                    public void run(Integer idPenjual) {
                        int id = idPenjual == null ? 0 : idPenjual;
                        Nav_push(ctx, idTransaksi, id);
                    }
                });
            }
        });

        // Row(mainAxisAlignment: spaceBetween, children: [Column(...), FutureBuilder(total)])
        LinearLayout row = Ui.row(ctx);
        addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout left = Ui.column(ctx);
        row.addView(left, Ui.weightW(1f));

        left.addView(Ui.text(ctx, status.toUpperCase(), new Txt.Style(hb * 3.5f)));
        left.addView(Ui.spaceH(ctx, 3.5f));
        left.addView(Ui.text(ctx, "Transaksi " + namaUser, teks));
        left.addView(Ui.spaceH(ctx, 10));
        left.addView(Ui.text(ctx, "Invoice No. " + nomorTransaksi, teks));
        left.addView(Ui.spaceH(ctx, 2));
        left.addView(Ui.text(ctx, "Pada. " + tanggalCheckout, teks));

        // FutureBuilder<int>(future: HistoriPenjualanModel.getTotalTransaksiForThisPenjual(idTransaksi), ...)
        final FrameLayout totalHolder = new FrameLayout(ctx);
        row.addView(totalHolder, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        totalHolder.addView(spinner(ctx));

        PesananScreenUi.run(ctx, new Async.Work<Integer>() {
            @Override
            public Integer run() {
                return HistoriPenjualanModel.getTotalTransaksiForThisPenjual(idTransaksi, null);
            }
        }, new Async.Done<Integer>() {
            @Override
            public void run(Integer totalTransaksiForThisPenjual) {
                if (totalTransaksiForThisPenjual == null) {
                    return; // snapshot has no data: the spinner stays
                }
                float hb2 = Ui.hb(ctx);
                TextView total = Ui.text(ctx, Currency.formatRupiah(totalTransaksiForThisPenjual, 0),
                        new Txt.Style(hb2 * 3.5f).color(CustomColors.green).bold());
                totalHolder.removeAllViews();
                totalHolder.addView(total, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            }
        });
    }

    /** SizedBox(width: 10, height: 10, child: CircularProgressIndicator(strokeWidth: 1)). */
    private static View spinner(Context ctx) {
        ProgressBar bar = new ProgressBar(ctx);
        bar.setIndeterminate(true);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.dp(ctx, 10), Ui.dp(ctx, 10), Gravity.CENTER);
        bar.setLayoutParams(lp);
        return bar;
    }

    /** Navigator.push(context, MaterialPageRoute(builder: (_) => ItemTransaksi(idTransaksi, idPenjual: ..., scaffold: true))). */
    private static void Nav_push(Context ctx, String idTransaksi, int idPenjual) {
        id.laukpauk.ui.Nav.push(ctx, ItemTransaksiActivity.class,
                Json.of("idTransaksi", idTransaksi, "idPenjual", String.valueOf(idPenjual), "scaffold", true));
    }
}
