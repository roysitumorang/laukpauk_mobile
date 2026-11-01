package id.laukpauk.view.pembeli;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.AppState;
import id.laukpauk.core.AuthModel;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.ui.Yuhu;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.penjual.TambahProdukActivity;
import id.laukpauk.view.pembeli.components.detailproduk.ProdukPenjualLainnya;

/**
 * Port of lib/views/pembeli/DetailProduk.dart.
 * Args: idProduk (String) - the Dart constructor field is called `id`, which is accepted as well.
 */
public class DetailProdukActivity extends BaseActivity {
    private String idProdukArg;

    private FrameLayout bodyHolder;
    private boolean loadedOnce = false;
    private boolean isButtonBeliBusy = false;
    private CustomButton.Btn beliButton;

    private static final class Loaded {
        boolean isLogin;
        String currentUserID;
        JsonElement detailProduk; // false = request failed
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idProdukArg = Json.has(args(), "idProduk") ? Json.str(args(), "idProduk") : Json.str(args(), "id");

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.nearlyWhite);
        root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        bodyHolder = new FrameLayout(this);
        bodyHolder.addView(Loading.view(this), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        root.addView(bodyHolder, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        load();
    }

    /** getDetailProduk() of the Dart state, then a rebuild. */
    private void load() {
        async(() -> {
            Loaded l = new Loaded();
            l.isLogin = AuthModel.isLogin();
            int currentUserIDInt = LocalStorage.getIdPenjualFromActiveUser();
            l.currentUserID = String.valueOf(currentUserIDInt);
            l.detailProduk = ProdukModel.getDetailProduk(idProdukArg);
            return l;
        }, data -> {
            loadedOnce = true;
            render(data);
        });
    }

    private void render(Loaded data) {
        final float hb = Ui.hb(this);
        bodyHolder.removeAllViews();
        beliButton = null;

        if (data == null || !Json.isObject(data.detailProduk)) {
            LinearLayout column = Ui.column(this);
            column.setGravity(Gravity.CENTER);
            column.addView(Ui.asset(this, "empty.png", 200));
            column.addView(Ui.spaceH(this, 20));
            column.addView(Ui.text(this, "Detail Produk Tidak Ditemukan", 16f, CustomColors.textDanger, true));
            bodyHolder.addView(DetailUi.refreshable(this, column, this::reload, true),
                    new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        final JsonObject detailProduk = data.detailProduk.getAsJsonObject();
        boolean hasKategori = Json.has(detailProduk, "namaKategoriProduk");
        String namaKategoriProduk = hasKategori ? Json.str(detailProduk, "namaKategoriProduk")
                : "Kategori Produk Tidak Diketahui";
        String keterangan = Json.str(detailProduk, "keterangan");
        final String idProduk = Json.str(detailProduk, "id");
        String foto = Json.str(detailProduk, "foto");
        boolean isItMyProduct = Json.str(detailProduk, "idPenjual").equals(data.currentUserID);
        boolean isDefaultImage = DefaultImage.defaultProduk.toLowerCase().equals(foto.toLowerCase());

        LinearLayout content = Ui.column(this);

        ImageView image = new ImageView(this);
        image.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetwork(image, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/" + foto);
        content.addView(image, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, hb * 60)));

        LinearLayout outer = Ui.column(this);
        Ui.padXY(outer, 2.5f, 5);
        LinearLayout info = Ui.column(this);
        Ui.padXY(info, 15, 15);
        outer.addView(info, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(outer, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout row = Ui.row(this);
        row.setGravity(Gravity.TOP);
        LinearLayout left = Ui.column(this);
        left.addView(Ui.text(this, Json.str(detailProduk, "nama").toUpperCase(), hb * 4.5f, CustomColors.green, true));
        left.addView(Ui.spaceH(this, 5));
        TextView kategori = Ui.text(this, namaKategoriProduk.toUpperCase(), hb * 3, CustomColors.textMuted);
        if (!hasKategori) {
            kategori.setTypeface(kategori.getTypeface(), android.graphics.Typeface.ITALIC);
        }
        left.addView(kategori);
        left.addView(Ui.spaceH(this, 15));

        LinearLayout tokoRow = Ui.row(this);
        tokoRow.setGravity(Gravity.CENTER_VERTICAL);
        tokoRow.addView(Ui.asset(this, "store.png", hb * 7.5f));
        tokoRow.addView(Ui.spaceW(this, 10));
        LinearLayout tokoCol = Ui.column(this);
        LinearLayout penjualRow = Ui.row(this);
        penjualRow.addView(Ui.text(this, "Penjual", hb * 3.5f, CustomColors.textMuted));
        penjualRow.addView(Ui.spaceW(this, 5));
        String namaToko = Json.str(detailProduk, "namaToko");
        TextView tokoName = Ui.text(this, namaToko.length() >= 1 ? namaToko : "Penjual Tidak Diketahui",
                hb * 3.5f, CustomColors.textInfo, true);
        final String idPenjual = Json.str(detailProduk, "idPenjual");
        tokoName.setOnClickListener(v -> Nav.push(this, PenjualActivity.class, Json.of("id", idPenjual)));
        penjualRow.addView(tokoName);
        tokoCol.addView(Layouts.hscroll(this, penjualRow), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        tokoCol.addView(Ui.text(this, "Klik nama penjual untuk melihat seluruh produk penjual", hb * 2.5f, 0xDD000000));
        tokoRow.addView(tokoCol, Ui.weightW(1f));
        left.addView(tokoRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        row.addView(left, Ui.weightW(1f));
        row.addView(Ui.spaceW(this, 10));

        final boolean mine = isItMyProduct;
        beliButton = CustomButton.button(this, isItMyProduct ? "Edit" : "Beli", () -> onBeliOrEdit(mine, idProduk, detailProduk),
                new CustomButton.Opts().busy(isButtonBeliBusy).color(
                        !isItMyProduct ? CustomButton.themeColor : CustomButton.warningColor).pill());
        row.addView(beliButton, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        info.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        info.addView(new Yuhu(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        info.addView(Ui.spaceH(this, 15));
        info.addView(Ui.text(this, "Deskripsi Produk", hb * 4, CustomColors.textMuted, true));
        info.addView(Ui.spaceH(this, 5));
        TextView desk = Ui.text(this, keterangan.length() > 0 ? keterangan : "Deskripsi tidak ada", hb * 3,
                CustomColors.textMuted);
        if (keterangan.length() == 0) {
            desk.setTypeface(desk.getTypeface(), android.graphics.Typeface.ITALIC);
        }
        info.addView(desk, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout lainnya = Ui.column(this);
        Ui.padXY(lainnya, 15, 10);
        lainnya.addView(Ui.text(this, "Produk Lainnya", hb * 3.75f, 0xDD000000, true));
        lainnya.addView(Ui.spaceH(this, 10));
        lainnya.addView(new ProdukPenjualLainnya(this, idProdukArg), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(lainnya, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        bodyHolder.addView(DetailUi.refreshable(this, content, this::reload, false),
                new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    /** RefreshIndicator onRefresh: setState(() {}) -> the future runs again. */
    private void reload() {
        load();
    }

    private void setBusy(boolean busy) {
        isButtonBeliBusy = busy;
        if (beliButton != null) {
            beliButton.setBusy(busy);
        }
    }

    private void onBeliOrEdit(boolean isItMyProduct, String idProduk, final JsonObject detailProduk) {
        if (!isItMyProduct) {
            boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
            if (isADJDSet) {
                setBusy(true);
                ProdukModel.showSatuanProduk(this, detailProduk, v -> setBusy(false));
            } else {
                ProdukModel.adjdNotSetAlert(this, tentukanADJD -> {
                    if (tentukanADJD != null && tentukanADJD) {
                        HeaderModel.adjd(this, null, null, null, (JsonObject adjdSet) -> {
                            String latitude = Json.strOrNull(adjdSet, "latitude");
                            String longitude = Json.strOrNull(adjdSet, "longitude");
                            String kodeJadwalDelivery = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

                            boolean adjdCompleted = false;
                            if (latitude != null && longitude != null && kodeJadwalDelivery != null) {
                                adjdCompleted = true;
                                ProdukModel.showSatuanProduk(this, detailProduk, null);
                            }
                            AppState.get().setADJDSet(adjdCompleted);
                        });
                    }
                });
            }
        } else {
            Nav.pushForResult(this, TambahProdukActivity.class,
                    Json.of("idProduk", idProduk, "jenisProduk", "satuan"), result -> {
                        if (result != null && !result.isJsonNull()) {
                            boolean statusEditProduk = Json.asBool(result, false);
                            String text = statusEditProduk ? "Berhasil mengubah data produk"
                                    : "Gagal mengubah data produk !";
                            DetailSnack.show(this, text, 2, 0f);
                            if (statusEditProduk) {
                                reload();
                            }
                        }
                    });
        }
    }
}
