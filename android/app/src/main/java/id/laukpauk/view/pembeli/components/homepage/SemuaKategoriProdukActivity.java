package id.laukpauk.view.pembeli.components.homepage;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.KategoriProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailUi;

/** Port of SemuaKategoriProduk (lib/views/pembeli/components/HomePage/SemuaKategoriProduk.dart), a pushed screen. */
public class SemuaKategoriProdukActivity extends BaseActivity {
    private JsonArray kategoriProdukState = new JsonArray();
    private JsonArray kategoriProdukImmutableState = new JsonArray();
    private boolean isReadyToShow = false;
    private String pencarian = "";

    private FrameLayout bodyHolder;
    private FrameLayout listHolder;
    private SwipeRefreshLayout refresh;
    private Pencarian pencarianView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        render();
        initialization();
    }

    private void initialization() {
        async(() -> KategoriProdukModel.getKategoriProduk(), kategoriProduk -> {
            boolean first = !isReadyToShow;
            isReadyToShow = true;
            kategoriProdukState = kategoriProduk;
            kategoriProdukImmutableState = kategoriProduk;
            if (first) {
                render();
            } else {
                renderList();
            }
        });
    }

    private void render() {
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(DetailUi.appBar(this, "Semua Kategori Produk"));

        bodyHolder = new FrameLayout(this);
        root.addView(bodyHolder, Ui.weightH(1f));

        if (isReadyToShow) {
            LinearLayout column = Ui.column(this);
            Ui.pad(column, 7.5f, 10, 7.5f, 10);
            pencarianView = new Pencarian(this, (String string) -> {
                String stringLC = string.toLowerCase();
                JsonArray filtered = new JsonArray();
                for (int i = 0; i < kategoriProdukImmutableState.size(); i++) {
                    JsonObject kp = Json.objAt(kategoriProdukImmutableState, i);
                    String namaLC = Json.str(kp, "nama").toLowerCase();
                    if (namaLC.indexOf(stringLC) >= 0) {
                        filtered.add(kp);
                    }
                }
                pencarian = stringLC;
                kategoriProdukState = filtered;
                pencarianView.setText(pencarian);
                renderList();
            }, () -> {
                pencarian = "";
                kategoriProdukState = kategoriProdukImmutableState;
                pencarianView.setText(pencarian);
                renderList();
            }, "Cari Kategori Produk di sini ..", pencarian);
            column.addView(pencarianView, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            column.addView(Ui.spaceH(this, 7.5f));
            listHolder = new FrameLayout(this);
            column.addView(listHolder, Ui.weightH(1f));
            bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            renderList();
        } else {
            bodyHolder.addView(DetailUi.smallSpinner(this, 25f), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        }
        setRoot(root);
    }

    private void renderList() {
        if (listHolder == null) {
            return;
        }
        float hb = Ui.hb(this);
        listHolder.removeAllViews();
        View content;
        boolean fill;
        if (kategoriProdukState.size() >= 1) {
            List<View> items = new ArrayList<>();
            for (int i = 0; i < kategoriProdukState.size(); i++) {
                items.add(new SemuaKategoriProdukItem(this, Json.objAt(kategoriProdukState, i)));
            }
            content = Layouts.grid(this, items, 3, 5.0f, 5.0f, 1f);
            fill = false;
        } else {
            LinearLayout center = HomeUi.emptyBlock(this, hb * 30, "Kategori Produk Tidak Ada", hb * 3.5f);
            center.setGravity(Gravity.CENTER);
            content = center;
            fill = true;
        }
        refresh = DetailUi.refreshable(this, content, () -> {
            initialization();
            refresh.setRefreshing(false);
        }, fill);
        listHolder.addView(refresh, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }
}
