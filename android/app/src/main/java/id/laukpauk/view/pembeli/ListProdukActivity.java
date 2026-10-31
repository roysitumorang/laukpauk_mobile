package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.homepage.HomeUi;
import id.laukpauk.view.pembeli.components.listproduk.ListPenjualItem;
import id.laukpauk.view.pembeli.components.listproduk.ListProdukItem;

/**
 * Port of lib/views/pembeli/ListProduk.dart. Args: title, subTitle (unused, as in Dart), dataProduk
 * (JsonArray), dataPenjual (JsonArray, optional), isReadyToShow (boolean).
 *
 * Dart's `onRefresh` callback is not passable. All three Dart callers (Kategori, Konten,
 * SemuaKategoriProdukItem) pass either null or `() async {}` / a re-fetch of data that ListProduk
 * copies at initState anyway, and ListProduk itself never invokes onRefresh ("sebelumnya ada
 * refreshindicator tapi gak work"), so there is nothing to reload: the parameter is dropped.
 */
public class ListProdukActivity extends BaseActivity {
    private JsonArray dataProdukState = new JsonArray();
    private JsonArray dataProdukImmutableState = new JsonArray();
    private JsonArray dataPenjualState = new JsonArray();
    private JsonArray dataPenjualImmutableState = new JsonArray();
    private String pencarian = "";
    private String shownView = "produk"; // produk || penjual

    private boolean isReadyToShow;
    private String title;

    private FrameLayout titleHolder;
    private FrameLayout listHolder;
    private Pencarian pencarianView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        JsonObject a = args();
        title = Json.str(a, "title");
        isReadyToShow = Json.bool(a, "isReadyToShow");
        JsonArray dataProduk = Json.array(a, "dataProduk");
        JsonArray dataPenjual = Json.array(a, "dataPenjual"); // null in Dart -> []

        dataProdukState = dataProduk;
        dataProdukImmutableState = dataProduk;
        dataPenjualState = dataPenjual;
        dataPenjualImmutableState = dataPenjual;

        render();
    }

    private void render() {
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.white);

        if (isReadyToShow) {
            root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            titleHolder = new FrameLayout(this);
            root.addView(titleHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            renderTitle();

            pencarianView = new Pencarian(this, (String string) -> {
                String stringLC = string.toLowerCase();

                if (shownView.equals("produk")) {
                    JsonArray filteredDataProduk = new JsonArray();
                    for (int i = 0; i < dataProdukImmutableState.size(); i++) {
                        JsonObject produk = Json.objAt(dataProdukImmutableState, i);
                        String namaProdukLC = Json.str(produk, "nama").toLowerCase();
                        if (namaProdukLC.indexOf(stringLC) >= 0) {
                            filteredDataProduk.add(produk);
                        }
                    }
                    pencarian = stringLC;
                    dataProdukState = filteredDataProduk;
                } else {
                    JsonArray filteredDataPenjual = new JsonArray();
                    for (int i = 0; i < dataPenjualImmutableState.size(); i++) {
                        JsonObject penjual = Json.objAt(dataPenjualImmutableState, i);
                        String namaPenjualLC = Json.str(penjual, "namaToko").toLowerCase();
                        if (namaPenjualLC.indexOf(stringLC) >= 0) {
                            filteredDataPenjual.add(penjual);
                        }
                    }
                    pencarian = stringLC;
                    dataPenjualState = filteredDataPenjual;
                }
                pencarianView.setText(pencarian);
                renderTitle();
                renderList();
            }, () -> {
                pencarian = "";
                if (shownView.equals("produk")) {
                    dataProdukState = dataProdukImmutableState;
                } else {
                    dataPenjualState = dataPenjualImmutableState;
                }
                pencarianView.setText(pencarian);
                renderTitle();
                renderList();
            }, "Cari Produk dengan Nama", pencarian);
            LinearLayout searchPad = Ui.column(this);
            Ui.padXY(searchPad, 15, 10);
            searchPad.addView(pencarianView, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            root.addView(searchPad, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            listHolder = new FrameLayout(this);
            root.addView(listHolder, Ui.weightH(1f));
            renderList();
        } else {
            FrameLayout center = new FrameLayout(this);
            center.addView(DetailUi.smallSpinner(this, 25), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            root.addView(center, Ui.weightH(1f));
        }

        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);
    }

    /** Title row + the store/produk switch icon. */
    private void renderTitle() {
        if (titleHolder == null) {
            return;
        }
        float hb = Ui.hb(this);
        titleHolder.removeAllViews();
        LinearLayout row = Ui.row(this);
        Ui.pad(row, 15, 15, 15, 2.5f);
        LinearLayout titleColumn = Ui.column(this);
        titleColumn.addView(Ui.text(this, title, hb * 4, 0xDD000000), new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        row.addView(titleColumn, Ui.weightW(1f));

        if (dataPenjualState.size() >= 1 && shownView.equals("produk")) {
            ImageView store = Ui.asset(this, "store.png", 35, 35);
            Ui.onTap(store, () -> {
                shownView = "penjual";
                renderTitle();
                renderList();
            });
            row.addView(store);
        }
        if (dataPenjualState.size() >= 1 && shownView.equals("penjual")) {
            ImageView produk = Ui.asset(this, "produk.png", 35, 35);
            Ui.onTap(produk, () -> {
                shownView = "produk";
                renderTitle();
                renderList();
            });
            row.addView(produk);
        }
        titleHolder.addView(row, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private void renderList() {
        if (listHolder == null) {
            return;
        }
        final BaseActivity act = this;
        float hb = Ui.hb(this);
        listHolder.removeAllViews();

        List<View> dataToShow = new ArrayList<>();
        if (shownView.equals("produk")) {
            for (int i = 0; i < dataProdukState.size(); i++) {
                final JsonObject produk = Json.objAt(dataProdukState, i);
                dataToShow.add(new ListProdukItem(this, produk, () -> HomeUi.pilihAdjd(act, produk), null));
            }
        } else {
            for (int i = 0; i < dataPenjualState.size(); i++) {
                JsonObject penjual = Json.objAt(dataPenjualState, i);
                dataToShow.add(new ListPenjualItem(this, penjual, () -> HomeUi.pilihAdjd(act, null), null));
            }
        }

        // quirk kept: the grid is chosen by dataProdukState (even in the 'penjual' view)
        if (dataProdukState.size() >= 1) {
            LinearLayout padded = Ui.column(this);
            Ui.padXY(padded, 15, 0);
            padded.addView(Layouts.grid(this, dataToShow, 2, hb * 1.5f, 0, 0.60f),
                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            ScrollView scroll = new ScrollView(this);
            scroll.addView(padded, new android.view.ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
            listHolder.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        } else {
            LinearLayout center = Ui.column(this);
            center.setGravity(Gravity.CENTER);
            Ui.padXY(center, hb * 15, 0);
            center.addView(Ui.asset(this, "empty.png", hb * 30));
            center.addView(Ui.spaceH(this, 5));
            TextView t = Ui.text(this,
                    "Tidak ada produk. Yuk mak, cari - cari lagi produk lainnya dan dapatkan produk yang mamak cari!",
                    hb * 3.25f, 0xDD000000);
            t.setGravity(Gravity.CENTER);
            t.setLineSpacing(0, 1.25f);
            center.addView(t, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            ScrollView scroll = new ScrollView(this);
            scroll.setFillViewport(true);
            scroll.addView(center, new android.view.ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
            listHolder.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        }
    }
}
