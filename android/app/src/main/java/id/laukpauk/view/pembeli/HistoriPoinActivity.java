package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.PointModel;
import id.laukpauk.model.UserModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;

/** Port of lib/views/pembeli/HistoriPoin.dart. */
public class HistoriPoinActivity extends BaseActivity {
    private LinearLayout listHolder;
    private SwipeRefreshLayout refresh;
    private int requestId = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        AccountUi.requireLogin(this, HistoriPoinActivity.class, new JsonObject(), false, this::render);
    }

    private void render() {
        float hb = Ui.hb(this);
        LinearLayout root = Ui.column(this);
        root.addView(DetailUi.appBar(this, "Histori Poin"), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout page = Ui.column(this);
        Ui.padXY(page, 5, 5);
        LinearLayout card = AccountUi.card(this, page);
        Ui.padXY(card, 15, 10);

        View title = Ui.text(this, "Poin Anda", hb * 4.5f, 0xDD000000, true);
        Ui.pad(title, 10, 10, 10, 5);
        card.addView(title, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

        listHolder = Ui.column(this);
        card.addView(listHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        refresh = DetailUi.refreshable(this, page, this::load, false);
        root.addView(refresh, Ui.weightH(1f));
        setRoot(root);

        load();
    }

    private void load() {
        requestId++;
        final int thisRequest = requestId;
        listHolder.removeAllViews();
        listHolder.addView(Loading.view(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        async(UserModel::getHistoriPoint, historiPoint -> {
            if (thisRequest != requestId) {
                return;
            }
            refresh.setRefreshing(false);
            if (historiPoint == null) {
                return; // Dart: FutureBuilder without data keeps the loader
            }
            listHolder.removeAllViews();
            float hb = Ui.hb(this);
            if (historiPoint.size() >= 1) {
                for (int i = 0; i < historiPoint.size(); i++) {
                    JsonObject histori = Json.objAt(historiPoint, i);
                    String keterangan = Json.str(histori, "keterangan");
                    String createdFrom = Json.str(histori, "createdFrom");

                    if (createdFrom.equals(PointModel.createdFrom_transaksi)) {
                        keterangan = "Transaksi " + keterangan;
                    }

                    long nilai = Json.asLong(Json.get(histori, "nilai"), 0);

                    // ListTile(leading: Image.asset(poin.png 35x35), title: Text(rupiah), subtitle: Text(keterangan))
                    LinearLayout tile = Ui.row(this);
                    Ui.padXY(tile, 16, 8);
                    tile.setMinimumHeight(Ui.dp(this, 72));
                    tile.addView(Ui.asset(this, "poin.png", 35f, 35f));
                    tile.addView(Ui.spaceW(this, 16));
                    LinearLayout texts = Ui.column(this);
                    texts.addView(Ui.text(this, Currency.formatRupiah(nilai, 0), 16f,
                            nilai >= 0 ? CustomColors.textSuccess : CustomColors.textDanger, true));
                    texts.addView(Ui.text(this, keterangan, 14f, 0x8A000000));
                    tile.addView(texts, Ui.weightW(1f));
                    listHolder.addView(tile, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
            } else {
                LinearLayout empty = Ui.column(this);
                empty.setGravity(Gravity.CENTER_HORIZONTAL);
                Ui.padXY(empty, 0, hb * 5f);
                empty.addView(Ui.asset(this, "empty.png", hb * 25f));
                empty.addView(Ui.text(this, "Tidak ada histori poin", 14f, 0xDD000000));
                listHolder.addView(empty, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        });
    }
}
