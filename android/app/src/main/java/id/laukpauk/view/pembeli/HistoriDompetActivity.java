package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.historidompet.HistoriDompetItem;

/** Port of lib/views/pembeli/HistoriDompet.dart. Args: jenisDompet (String, optional). */
public class HistoriDompetActivity extends BaseActivity {
    // 0 = semua jenis dompet, 1 = jenis dompet referral, 2 = jenis dompet potongantransaksi
    private int jenisDompet = 0;

    private LinearLayout tabRow;
    private LinearLayout listHolder;
    private SwipeRefreshLayout refresh;
    private int requestId = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        String jenisDompetFromWidget = Json.strOrNull(args(), "jenisDompet");
        if (jenisDompetFromWidget != null) {
            int jenisDompetInt = 0;
            if (jenisDompetFromWidget.toLowerCase().equals("referral")) {
                jenisDompetInt = 1;
            }
            if (jenisDompetFromWidget.toLowerCase().equals("potongantransaksi")) {
                jenisDompetInt = 2;
            }
            jenisDompet = jenisDompetInt;
        }

        // Dart: Login(routeTo: HistoriDompet()) - the constructor argument is not carried over.
        AccountUi.requireLogin(this, HistoriDompetActivity.class, new JsonObject(), false, this::render);
    }

    private JsonArray getRiwayatDompet(int jenis) {
        String jenisDompetString = "";

        if (jenis == 1) {
            jenisDompetString = "jenisDompet=referral";
        }

        if (jenis == 2) {
            jenisDompetString = "jenisDompet=potongantransaksi";
        }

        String params = HTTPRequest.getQueryString(jenisDompetString, true);

        Http.Response httpResponse = Http.getApi("/User/getRiwayatDompet?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "riwayatDompet");
        }
        return new JsonArray();
    }

    private void render() {
        LinearLayout root = Ui.column(this);
        root.addView(DetailUi.appBar(this, "Histori Dompet"), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout page = Ui.column(this);
        Ui.padXY(page, 5, 5);
        LinearLayout card = AccountUi.card(this, page);
        Ui.padXY(card, 15, 10);

        tabRow = Ui.row(this);
        HorizontalScrollView tabScroll = Layouts.hscroll(this, tabRow);
        Ui.padXY(tabScroll, 10, 0);
        card.addView(tabScroll, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 25));

        listHolder = Ui.column(this);
        card.addView(listHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        refresh = DetailUi.refreshable(this, page, this::loadList, false);
        root.addView(refresh, Ui.weightH(1f));
        setRoot(root);

        buildTabs();
        loadList();
    }

    private void buildTabs() {
        tabRow.removeAllViews();
        String[] labels = {"Semua", "Referral", "Potongan Transaksi"};
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            boolean selected = (i == jenisDompet);
            LinearLayout tab = Ui.column(this);
            tab.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView label = Ui.text(this, labels[i], Ui.hb(this) * 3.5f,
                    selected ? CustomColors.green : CustomColors.textMuted);
            Ui.padXY(label, 16, 12);
            tab.addView(label, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            View indicator = new View(this);
            indicator.setBackgroundColor(selected ? CustomColors.green : CustomColors.transparent);
            tab.addView(indicator, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, 2)));
            Ui.onTap(tab, () -> {
                jenisDompet = index;
                buildTabs();
                loadList();
            });
            tabRow.addView(tab, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }
    }

    private void loadList() {
        requestId++;
        final int thisRequest = requestId;
        final int jenis = jenisDompet;

        listHolder.removeAllViews();
        View loading = Loading.view(this, null, null, 20f, 20f);
        Ui.pad(loading, 0, 0, 0, 25);
        listHolder.addView(loading, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        async(() -> getRiwayatDompet(jenis), riwayatDompet -> {
            if (thisRequest != requestId) {
                return;
            }
            refresh.setRefreshing(false);
            listHolder.removeAllViews();
            if (riwayatDompet == null) {
                // Dart FutureBuilder without data keeps showing the loader
                View again = Loading.view(this, null, null, 20f, 20f);
                Ui.pad(again, 0, 0, 0, 25);
                listHolder.addView(again, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                return;
            }
            if (riwayatDompet.size() >= 1) {
                int nomor = 1;
                for (int i = 0; i < riwayatDompet.size(); i++) {
                    int indexData = nomor;
                    nomor++;
                    listHolder.addView(new HistoriDompetItem(this, indexData, Json.objAt(riwayatDompet, i)),
                            new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
            } else {
                float hb = Ui.hb(this);
                LinearLayout empty = Ui.column(this);
                empty.setGravity(Gravity.CENTER_HORIZONTAL);
                Ui.pad(empty, 0, 0, 0, 15);
                empty.addView(Ui.asset(this, "empty.png", hb * 30));
                empty.addView(Ui.spaceH(this, 10));
                empty.addView(Ui.text(this, "Tidak ada riwayat dompet", hb * 3.5f, CustomColors.textMuted));
                listHolder.addView(empty, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        });
    }
}
