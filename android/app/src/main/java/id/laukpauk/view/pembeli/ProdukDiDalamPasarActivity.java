package id.laukpauk.view.pembeli;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.PasarModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.components.produkdidalampasar.PasarLain;
import id.laukpauk.view.pembeli.components.produkdidalampasar.ProdukDiDalamPasarItem;

/**
 * Port of lib/views/pembeli/ProdukDiDalamPasar.dart.
 * Args: idPasar (String), namaPasar (String, default 'Pasar Tidak di Kenal').
 */
public class ProdukDiDalamPasarActivity extends BaseActivity {
    private String idPasar;
    private String namaPasar;

    private String pencarian;
    private boolean searchTyped = false;

    private int startFrom = 0; // 0 karena akan ditambah 10 ketika initialization() dijalankan
    private int startFromSearch = 10; // langsung 10 karena tidak diset ketika initialization
    private JsonArray dataProdukState = new JsonArray();
    private JsonArray dataProdukStateImmutable = new JsonArray();
    private boolean isFetchingData = false;

    private FrameLayout gridHolder;
    private SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idPasar = Json.str(args(), "idPasar");
        namaPasar = Json.has(args(), "namaPasar") ? Json.str(args(), "namaPasar") : "Pasar Tidak di Kenal";
        buildScreen();
        initialization();
    }

    private void initialization() {
        final String startFromString = String.valueOf(startFrom);
        async(() -> PasarModel.getProdukInThisPasar(idPasar, "y", startFromString, null), dataProduk -> {
            JsonArray data = dataProduk != null ? dataProduk : new JsonArray();
            dataProdukState = data;
            dataProdukStateImmutable = data;
            startFrom = startFrom + 10;
            renderGrid();
        });
    }

    /** The scroll listener of the Dart GridView: reached the end -> load the next 10. */
    private void onGridEnd() {
        if (isFetchingData) {
            return;
        }
        String cariProduk = null;
        boolean isSearching = false;
        if (pencarian != null) {
            if (pencarian.length() >= 1) {
                cariProduk = pencarian;
                isSearching = true;
            }
        }

        isFetchingData = true;
        renderGrid();
        final boolean searching = isSearching;
        final String cari = cariProduk;
        final String from = searching ? String.valueOf(startFromSearch) : String.valueOf(startFrom);
        async(() -> PasarModel.getProdukInThisPasar(idPasar, "y", from, cari), nextDataProduk -> {
            JsonArray next = nextDataProduk != null ? nextDataProduk : new JsonArray();
            if (searching) {
                startFromSearch = startFromSearch + 10;
            } else {
                startFrom = startFrom + 10;
            }
            isFetchingData = false;
            JsonArray merged = new JsonArray();
            JsonArray mergedImmutable = new JsonArray();
            for (JsonElement e : dataProdukState) {
                merged.add(e);
            }
            for (JsonElement e : next) {
                merged.add(e);
            }
            for (JsonElement e : dataProdukStateImmutable) {
                mergedImmutable.add(e);
            }
            for (JsonElement e : next) {
                mergedImmutable.add(e);
            }
            dataProdukState = merged;
            dataProdukStateImmutable = mergedImmutable;
            renderGrid();
        });
    }

    private void buildScreen() {
        final float hb = Ui.hb(this);
        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout content = Ui.column(this);

        TextView title = Ui.text(this, "Produk di Pasar " + namaPasar, hb * 4, 0xDD000000, true);
        Ui.padXY(title, 10, 10);
        content.addView(title, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(Ui.spaceH(this, 5));

        Pencarian pencarianView = new Pencarian(this, string -> {
            hideKeyboard();
            if (string.length() >= 1) {
                async(() -> PasarModel.getProdukInThisPasar(idPasar, "y", null, string), fetched -> {
                    pencarian = string;
                    searchTyped = true;
                    dataProdukState = fetched != null ? fetched : new JsonArray();
                    renderGrid();
                });
            } else {
                pencarian = string;
                searchTyped = true;
                dataProdukState = dataProdukStateImmutable;
                renderGrid();
            }
        }, () -> {
            pencarian = null;
            searchTyped = false;
            dataProdukState = dataProdukStateImmutable;
            renderGrid();
        }, "Cari Produk di Sini", pencarian != null ? pencarian : "");
        LinearLayout pencarianHolder = Ui.column(this);
        Ui.padXY(pencarianHolder, 10, 0);
        pencarianHolder.addView(pencarianView, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(pencarianHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(Ui.spaceH(this, 10));

        gridHolder = new FrameLayout(this);
        LinearLayout gridPad = Ui.column(this);
        Ui.padXY(gridPad, 10, 0);
        gridPad.addView(gridHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, Ui.vb(this) * 50)));
        content.addView(gridPad, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        content.addView(Ui.spaceH(this, 10));
        content.addView(new PasarLain(this, idPasar), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        refresh = DetailUi.refreshable(this, content, () -> {
            renderGrid(); // setState(() {})
            refresh.setRefreshing(false);
        }, false);
        root.addView(refresh, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);
        renderGrid();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void renderGrid() {
        final float hb = Ui.hb(this);
        String tidakAdaProduk = "Tunggu sebentar ya mak...";
        if (searchTyped) {
            tidakAdaProduk = "Maaf Mak...Produk yang dicari belum ada di pasar ini";
        }
        gridHolder.removeAllViews();

        if (dataProdukState.size() >= 1) {
            List<View> children = new ArrayList<>();
            for (int i = 0; i < dataProdukState.size(); i++) {
                final JsonObject dataProduk = Json.objAt(dataProdukState, i);
                children.add(new ProdukDiDalamPasarItem(this, dataProduk, () -> {
                    ADJDModel.deleteAlamatDanJadwalDelivery();
                    HeaderModel.adjd(this, null, null, null, (JsonObject adjdSet) -> {
                        String latitude = Json.strOrNull(adjdSet, "latitude");
                        String longitude = Json.strOrNull(adjdSet, "longitude");
                        String kodeJadwalDelivery = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

                        if (latitude != null && longitude != null && kodeJadwalDelivery != null) {
                            AppState.get().setADJDSet(true);
                            ProdukModel.showSatuanProduk(this, dataProduk, null);
                        }
                    });
                }, (double) (hb * 25)));
            }
            LinearLayout grid = Layouts.grid(this, children, 3, 0f, 0f, 0.45f);

            final ScrollView scroll = new ScrollView(this);
            scroll.setVerticalScrollBarEnabled(false);
            scroll.addView(grid, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            // The grid scrolls on its own inside the screen's list (nested scrolling).
            scroll.setOnTouchListener((v, ev) -> {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            });
            scroll.setOnScrollChangeListener((v, scrollX, scrollY, oldX, oldY) -> {
                View child = scroll.getChildAt(0);
                if (child != null && child.getBottom() - (scroll.getHeight() + scroll.getScrollY()) <= 0) {
                    onGridEnd();
                }
            });
            gridHolder.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

            if (isFetchingData) {
                LinearLayout overlay = Ui.column(this);
                overlay.setGravity(Gravity.CENTER);
                overlay.setBackground(Ui.shape(this, 0xCFFFFFFF, 10f));
                overlay.setClickable(true);
                overlay.addView(Ui.asset(this, "loading.png", hb * 35));
                overlay.addView(Ui.spaceH(this, 10));
                TextView t1 = Ui.text(this, "Sedang Mem-proses", hb * 4, 0xDD000000, true);
                t1.setGravity(Gravity.CENTER);
                overlay.addView(t1, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
                overlay.addView(Ui.spaceH(this, 5));
                TextView t2 = Ui.text(this, "Tunggu sebentar ya...", hb * 3.25f, CustomColors.textMuted);
                t2.setGravity(Gravity.CENTER);
                overlay.addView(t2, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
                overlay.addView(Ui.spaceH(this, 15));
                ProgressBar bar = new ProgressBar(this);
                bar.setIndeterminate(true);
                bar.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(CustomColors.green));
                overlay.addView(bar, new LinearLayout.LayoutParams(Ui.dp(this, hb * 5), Ui.dp(this, hb * 5)));
                gridHolder.addView(overlay, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            }
        } else {
            LinearLayout empty = Ui.column(this);
            empty.setGravity(Gravity.CENTER);
            Ui.padXY(empty, hb * 10, 0);
            if (searchTyped) {
                empty.addView(Ui.asset(this, "empty.png", hb * 35));
            } else {
                empty.addView(Loading.view(this), new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            }
            empty.addView(Ui.spaceH(this, 15));
            TextView t = Ui.text(this, tidakAdaProduk, hb * 3.25f, 0xDD000000);
            t.setGravity(Gravity.CENTER);
            empty.addView(t, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            gridHolder.addView(empty, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        }
    }
}
