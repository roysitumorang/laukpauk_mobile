package id.laukpauk.view.penjual;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.HistoriPenjualanModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.components.historipenjualan.HistoriPenjualanItem;

/** Port of lib/views/penjual/HistoriPenjualan.dart. Args: idPenjual (String, optional). */
public class HistoriPenjualanActivity extends BaseActivity {
    private Integer statusTransaksi; // null until a tab is tapped
    private String idPenjual;
    private int generation = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idPenjual = Json.strOrNull(args(), "idPenjual");
        render();
    }

    private void render() {
        final int myGeneration = ++generation;
        final float hb = Ui.hb(this);

        String statusTransaksiString = null;
        if (statusTransaksi != null && statusTransaksi == 2) {
            statusTransaksiString = "received";
        }

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(PesananScreenUi.appBar(this, "Histori Penjualan Saya"),
                new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout body = Ui.column(this);
        Ui.pad(body, 10, 10, 10, 10);
        root.addView(body, Ui.weightH(1f));

        LinearLayout card = PesananScreenUi.card(this);
        body.addView(card);
        LinearLayout tabBox = Ui.column(this);
        tabBox.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(tabBox, 10, 5, 10, 5);
        card.addView(tabBox, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        tabBox.addView(PesananScreenUi.tabBar(this, new String[]{"Hari Ini", "Semua", "Completed"},
                statusTransaksi == null ? 0 : statusTransaksi, hb * 3.25f, (Integer value) -> {
                    statusTransaksi = value;
                    render();
                }));
        body.addView(Ui.spaceH(this, 3.5f));

        final FrameLayout listSlot = new FrameLayout(this);
        listSlot.addView(DetailUi.smallSpinner(this, 25), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        body.addView(listSlot, Ui.weightH(1f));

        final String select = "namaUser, waktuCheckout, nomorTransaksi, status, id";
        final String status = statusTransaksiString;
        async(() -> HistoriPenjualanModel.getHistoriPenjualan(idPenjual, select, status),
                (JsonArray historiPenjualan) -> {
                    if (myGeneration != generation || historiPenjualan == null) {
                        return;
                    }
                    listSlot.removeAllViews();
                    if (historiPenjualan.size() >= 1) {
                        LinearLayout list = Ui.column(this);
                        for (JsonElement penjualan : historiPenjualan) {
                            list.addView(new HistoriPenjualanItem(this, Json.obj(penjualan)),
                                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                        }
                        SwipeRefreshLayout refresh = DetailUi.refreshable(this, list, () -> render(), false);
                        listSlot.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
                    } else {
                        LinearLayout empty = Ui.column(this);
                        empty.setGravity(Gravity.CENTER);
                        empty.addView(Ui.asset(this, "empty.png", hb * 35));
                        empty.addView(Ui.spaceH(this, 10));
                        empty.addView(Ui.text(this, "Histori transaksi tidak ditemukan", hb * 3.5f,
                                CustomColors.textMuted));
                        SwipeRefreshLayout refresh = DetailUi.refreshable(this, empty, () -> render(), true);
                        listSlot.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
                    }
                });

        setRoot(root);
    }
}
