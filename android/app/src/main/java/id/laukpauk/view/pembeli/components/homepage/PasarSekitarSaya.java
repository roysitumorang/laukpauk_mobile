package id.laukpauk.view.pembeli.components.homepage;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.model.ADJDModel;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.HomepageModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Pencarian;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.SemuaPasarActivity;

/**
 * Port of lib/views/pembeli/components/HomePage/PasarSekitarSaya.dart. Dart wraps the markets in a
 * Consumer&lt;HeaderViewModel&gt;: the list is fetched again whenever the header state changes (here:
 * isADJDSet / savedADJD) or the search text changes.
 */
public class PasarSekitarSaya extends LinearLayout {
    private final boolean showPilihLokasiLain;
    private final boolean showTitle;
    private final boolean showAll;
    private final boolean showPencarian;
    private final boolean showProdukPasarSekitar;

    private String pencarian = "";
    private boolean built = false;
    private int requestCounter = 0;
    private String lastKey = null;

    private FrameLayout bodyHolder;
    private LinearLayout produkHolder;
    private Pencarian pencarianView;

    private final AppState.Listener listener = new AppState.Listener() {
        @Override
        public void onChanged() {
            if (!stateKey().equals(lastKey)) {
                load();
            }
        }
    };

    public PasarSekitarSaya(Context ctx, boolean showPilihLokasiLain, boolean showTitle, boolean showAll,
                            boolean showPencarian, boolean showProdukPasarSekitar) {
        super(ctx);
        this.showPilihLokasiLain = showPilihLokasiLain;
        this.showTitle = showTitle;
        this.showAll = showAll;
        this.showPencarian = showPencarian;
        this.showProdukPasarSekitar = showProdukPasarSekitar;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        showLoading();
        load();
    }

    /** PasarSekitarSaya() with the Dart defaults. */
    public PasarSekitarSaya(Context ctx) {
        this(ctx, true, true, false, false, true);
    }

    private String stateKey() {
        AppState s = AppState.get();
        return s.isADJDSet() + "|" + (s.getSavedADJD() == null ? "" : Json.toJson(s.getSavedADJD()));
    }

    private void showLoading() {
        Context ctx = getContext();
        float hb = Ui.hb(ctx);
        removeAllViews();
        built = false;
        LinearLayout box = Ui.column(ctx);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(0, Ui.dp(ctx, 25), 0, Ui.dp(ctx, 25));
        box.addView(Loading.view(ctx));
        box.addView(Ui.spaceH(ctx, 10));
        box.addView(HomeUi.t(ctx, "Sedang Mencari Pasar ..", hb * 3.25f));
        addView(box, new LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private void load() {
        lastKey = stateKey();
        final boolean isADJDSet = AppState.get().isADJDSet();
        final JsonObject savedADJD = AppState.get().getSavedADJD();
        final String search = pencarian;
        final int mine = ++requestCounter;
        HomeUi.act(getContext()).async(
                () -> HomepageModel.getDataPasar(isADJDSet, showAll, 2, search, savedADJD),
                result -> {
                    if (mine != requestCounter) {
                        return;
                    }
                    show(result, isADJDSet);
                });
    }

    private void buildFrame() {
        final Context ctx = getContext();
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);
        removeAllViews();

        if (showTitle) {
            LinearLayout titleRow = Ui.row(ctx);
            titleRow.setPadding(0, 0, 0, Ui.dp(ctx, 15));
            titleRow.addView(HomeUi.sectionTitle(ctx, "Pasar"));
            titleRow.addView(Ui.spacer(ctx));
            TextView lihat = Ui.text(ctx, "Lihat semua", hb * 3.0f, CustomColors.darkGreen);
            Ui.onTap(lihat, () -> Nav.push(ctx, SemuaPasarActivity.class));
            titleRow.addView(lihat);
            addView(titleRow, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        if (showPencarian) {
            pencarianView = new Pencarian(ctx, (String string) -> {
                String stringLC = string.toLowerCase();
                pencarian = stringLC;
                pencarianView.setText(pencarian);
                load();
            }, () -> {
                pencarian = "";
                pencarianView.setText(pencarian);
                load();
            }, "Cari Pasar dengan Nama", pencarian);
            addView(pencarianView, new LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        addView(Ui.spaceH(ctx, showPencarian ? 10.0f : 0));

        bodyHolder = new FrameLayout(ctx);
        addView(bodyHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));

        if (showPilihLokasiLain) {
            addView(CustomButton.button(ctx, "Pilih Lokasi Lain", () -> {
                ADJDModel.deleteAlamatDanJadwalDelivery();
                HeaderModel.adjd(act, null, null, null, null);
            }, new CustomButton.Opts().size(CustomButton.smSize).pill().color(CustomButton.themeColor)),
                    new LayoutParams(Ui.WRAP, Ui.WRAP));
        }
        addView(Ui.spaceH(ctx, showPilihLokasiLain ? 25.0f : 0.0f));

        produkHolder = Ui.column(ctx);
        addView(produkHolder, new LayoutParams(Ui.MATCH, Ui.WRAP));
        built = true;
    }

    private void show(JsonArray pasarSekitarSaya, boolean isADJDSet) {
        final Context ctx = getContext();
        float hb = Ui.hb(ctx);
        if (!built) {
            buildFrame();
        }
        if (pasarSekitarSaya == null) {
            pasarSekitarSaya = new JsonArray();
        }
        boolean showJarakPasar = isADJDSet;
        boolean isPasarExist = pasarSekitarSaya.size() >= 1;

        bodyHolder.removeAllViews();
        produkHolder.removeAllViews();

        if (isPasarExist) {
            LinearLayout container = Ui.column(ctx);
            container.setPadding(0, 0, 0, Ui.dp(ctx, 10));
            FlowLayout wrap = new FlowLayout(ctx, 1, 2.5f, false);
            for (int i = 0; i < pasarSekitarSaya.size(); i++) {
                JsonObject pasar = Json.objAt(pasarSekitarSaya, i);
                Pasar item = new Pasar(ctx, pasar, showJarakPasar);
                FrameLayout cell = new FrameLayout(ctx);
                cell.addView(item, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
                wrap.addView(cell, new android.view.ViewGroup.LayoutParams(Ui.dp(ctx, hb * 23.3f),
                        Ui.dp(ctx, hb * 23.3f)));
            }
            container.addView(wrap, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            container.addView(Ui.spaceH(ctx, 7.5f));
            bodyHolder.addView(container, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        } else {
            LinearLayout container = Ui.column(ctx);
            container.setGravity(Gravity.CENTER_HORIZONTAL);
            Ui.pad(container, hb * 20.0f, 15, hb * 20.0f, 15);
            container.addView(Ui.asset(ctx, "empty.png", hb * 25.0f));
            container.addView(Ui.spaceH(ctx, 10));
            TextView t = Ui.text(ctx, "Yahh! Belum ada pasar disekitar lokasimu mak. Maaf ya mak...",
                    hb * 3.25f, CustomColors.textMuted);
            t.setGravity(Gravity.CENTER);
            container.addView(t, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            container.addView(Ui.spaceH(ctx, 10));
            bodyHolder.addView(container, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        if (isPasarExist && showProdukPasarSekitar) {
            produkHolder.addView(new ProdukPasarSekitar(ctx, pasarSekitarSaya),
                    new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AppState.get().addListener(listener);
        if (built && !stateKey().equals(lastKey)) {
            load();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        AppState.get().removeListener(listener);
        super.onDetachedFromWindow();
    }
}
