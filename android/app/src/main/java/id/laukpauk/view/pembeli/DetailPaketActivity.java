package id.laukpauk.view.pembeli;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.Async;
import id.laukpauk.core.AppState;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Currency;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.PaketModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Footer;
import id.laukpauk.ui.Header;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.ui.Yuhu;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.penjual.TambahProdukActivity;
import id.laukpauk.view.pembeli.components.detailproduk.PaketPenjualLainnya;

/**
 * Port of lib/views/pembeli/DetailPaket.dart.
 * Args: idPaket (String).
 */
public class DetailPaketActivity extends BaseActivity {
    private String idPaket;
    private String currentUserIDState = "";
    private FrameLayout bodyHolder;

    private static final class Loaded {
        String currentUserID;
        JsonElement detailPaket;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        idPaket = Json.has(args(), "idPaket") ? Json.str(args(), "idPaket") : Json.str(args(), "id");

        LinearLayout root = Ui.column(this);
        root.setBackgroundColor(CustomColors.aliceBlue);
        root.addView(new Header(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        bodyHolder = new FrameLayout(this);
        bodyHolder.addView(DetailUi.smallSpinner(this, 15), new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        root.addView(bodyHolder, Ui.weightH(1f));
        root.addView(new Footer(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setRoot(root);

        load();
    }

    private void load() {
        async(() -> {
            Loaded l = new Loaded();
            l.currentUserID = LocalStorage.getCurrentUserID();
            l.detailPaket = PaketModel.detailPaket(idPaket);
            return l;
        }, data -> {
            currentUserIDState = data.currentUserID != null ? data.currentUserID : "";
            render(data.detailPaket);
        });
    }

    private void render(JsonElement detailPaketElement) {
        final float hb = Ui.hb(this);
        bodyHolder.removeAllViews();

        if (!Json.isObject(detailPaketElement)) {
            LinearLayout column = Ui.column(this);
            column.setGravity(Gravity.CENTER);
            column.addView(Ui.asset(this, "empty.png", 200));
            column.addView(Ui.spaceH(this, 20));
            column.addView(Ui.text(this, "Detail Produk Tidak Ditemukan", 16f, CustomColors.textDanger, true));
            bodyHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
            return;
        }

        final JsonObject detailPaket = detailPaketElement.getAsJsonObject();
        final String idPenjual = Json.str(detailPaket, "idPenjual");
        String foto = Json.str(detailPaket, "foto");
        String nama = Json.str(detailPaket, "nama");
        long harga = Json.parseInt(Json.str(detailPaket, "harga"), 0);
        long diskon = Json.parseInt(Json.str(detailPaket, "diskon"), 0);
        boolean adaKeterangan = Json.has(detailPaket, "keterangan") && Json.str(detailPaket, "keterangan").length() >= 1;
        String keterangan = adaKeterangan ? Json.str(detailPaket, "keterangan") : "Deskripsi Produk Tidak Ada";
        String namaPenjual = Json.str(detailPaket, "namaToko");
        boolean isItMyProduct = currentUserIDState.equals(idPenjual);
        long hargaBersih = harga - diskon;
        double persentaseDiskon = 0;
        if (diskon >= 1 && harga >= 1) {
            persentaseDiskon = (double) diskon / harga * 100;
        }
        boolean isDefaultImage = DefaultImage.defaultPaket.toLowerCase().equals(foto.toLowerCase());

        LinearLayout content = Ui.column(this);

        ImageView image = new ImageView(this);
        image.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetwork(image, ServerPath.baseURL + "/" + ServerPath.uploadGambarPaket + "/" + foto);
        content.addView(image, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(this, hb * 60)));

        LinearLayout outer = Ui.column(this);
        Ui.padXY(outer, 2.5f, 5);
        LinearLayout card = Layouts.card(this, CustomColors.nearlyWhite, 10f, 1f);
        Ui.padXY(card, 15, 15);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        cardLp.setMargins(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        outer.addView(card, cardLp);
        content.addView(outer, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout row = Ui.row(this);
        LinearLayout left = Ui.column(this);
        left.addView(Ui.text(this, nama.toUpperCase(), hb * 4.5f, CustomColors.green, true));
        left.addView(Ui.spaceH(this, 5));
        if (persentaseDiskon >= 0.1) {
            LinearLayout diskonRow = Ui.row(this);
            diskonRow.setGravity(Gravity.CENTER_VERTICAL);
            TextView coret = Ui.text(this, Currency.formatRupiah(harga, 0), hb * 3.5f, CustomColors.textMuted, true);
            coret.setPaintFlags(coret.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            diskonRow.addView(coret);
            TextView pill = Ui.text(this, "Diskon " + String.format(Locale.US, "%.1f", persentaseDiskon) + " %",
                    hb * 3, 0xDD000000);
            Ui.padXY(pill, 10, 5);
            pill.setBackground(Ui.shape(this, 0xFFFFC107, 50f));
            LinearLayout.LayoutParams pillLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            pillLp.leftMargin = Ui.dp(this, 10);
            diskonRow.addView(pill, pillLp);
            left.addView(diskonRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }
        left.addView(Ui.text(this, Currency.formatRupiah(hargaBersih, 0), hb * 4.0f, CustomColors.green, true));
        left.addView(Ui.spaceH(this, 15));

        LinearLayout tokoRow = Ui.row(this);
        tokoRow.setGravity(Gravity.CENTER_VERTICAL);
        tokoRow.addView(Ui.asset(this, "store.png", hb * 7.5f));
        tokoRow.addView(Ui.spaceW(this, 10));
        LinearLayout tokoCol = Ui.column(this);
        LinearLayout penjualRow = Ui.row(this);
        penjualRow.addView(Ui.text(this, "Penjual", hb * 3.5f, CustomColors.textMuted));
        penjualRow.addView(Ui.spaceW(this, 5));
        TextView tokoName = Ui.text(this, namaPenjual.length() >= 1 ? namaPenjual : "Penjual Tidak Diketahui",
                hb * 3.5f, CustomColors.textInfo, true);
        tokoName.setOnClickListener(v -> Nav.push(this, PenjualActivity.class, Json.of("id", idPenjual)));
        penjualRow.addView(tokoName);
        tokoCol.addView(Layouts.hscroll(this, penjualRow), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        tokoCol.addView(Ui.text(this, "Klik nama penjual untuk melihat seluruh produk penjual", hb * 2.5f, 0xDD000000));
        tokoRow.addView(tokoCol, Ui.weightW(1f));
        left.addView(tokoRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        row.addView(left, Ui.weightW(1f));
        row.addView(Ui.spaceW(this, 10));

        final boolean mine = isItMyProduct;
        row.addView(CustomButton.button(this, !isItMyProduct ? "Beli" : "Edit", () -> onBeliOrEdit(mine),
                new CustomButton.Opts().color(!isItMyProduct ? CustomButton.themeColor : CustomButton.warningColor).pill()),
                new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        card.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        card.addView(new Yuhu(this), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(Ui.spaceH(this, 15));
        card.addView(Ui.text(this, "Deskripsi Paket", hb * 4, CustomColors.textMuted, true));
        card.addView(Ui.spaceH(this, 5));
        TextView desk = Ui.text(this, keterangan, hb * 3, CustomColors.textMuted);
        if (!adaKeterangan) {
            desk.setTypeface(desk.getTypeface(), android.graphics.Typeface.ITALIC);
        }
        card.addView(desk, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        LinearLayout lainnya = Ui.column(this);
        Ui.padXY(lainnya, 15, 10);
        lainnya.addView(Ui.text(this, "Paket Lainnya", hb * 3.75f, 0xDD000000, true));
        lainnya.addView(Ui.spaceH(this, 10));
        lainnya.addView(new PaketPenjualLainnya(this, idPaket), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content.addView(lainnya, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.addView(content, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        bodyHolder.addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
    }

    private void updateJumlahCart() {
        Async.run(() -> HeaderModel.getJumlahCart(), (Integer jumlahKeranjang) -> {
            if (jumlahKeranjang != null) {
                AppState.get().setJumlahCart(jumlahKeranjang);
            }
        });
    }

    private void onBeliOrEdit(boolean isItMyProduct) {
        if (!isItMyProduct) {
            boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
            if (isADJDSet) {
                async(() -> PaketModel.addToCart(idPaket), statusAddToCart -> {
                    if (statusAddToCart != null && statusAddToCart) {
                        String snackbarText = "Berhasil menambahkan paket ke dalam keranjang";
                        PaketModel.addToCartSnackBar(this, snackbarText);
                        updateJumlahCart();
                    }
                });
            } else {
                ProdukModel.adjdNotSetAlert(this, tentukanADJD -> {
                    if (tentukanADJD != null && tentukanADJD) {
                        HeaderModel.adjd(this, null, null, null, (JsonObject adjdSet) -> {
                            String latitude = Json.strOrNull(adjdSet, "latitude");
                            String longitude = Json.strOrNull(adjdSet, "longitude");
                            String jadwalDeliveryCode = Json.strOrNull(adjdSet, "kodeJadwalDelivery");

                            if (latitude != null && longitude != null && jadwalDeliveryCode != null) {
                                async(() -> PaketModel.addToCart(idPaket), statusAddToCart -> {
                                    boolean ok = statusAddToCart != null && statusAddToCart;
                                    String text = ok ? "Produk berhasil ditambahkan ke keranjang !"
                                            : "Produk gagal ditambahkan ke keranjang, silahkan coba lagi.";
                                    DetailSnack.show(this, text, 1, Ui.hb(this) * 3.25f);
                                    updateJumlahCart();
                                    AppState.get().setADJDSet(true);
                                });
                            }
                        });
                    }
                });
            }
        } else {
            Nav.pushForResult(this, TambahProdukActivity.class,
                    Json.of("idProduk", idPaket, "jenisProduk", "paket"), result -> {
                        if (result != null && !result.isJsonNull()) {
                            boolean statusEditPaket = Json.asBool(result, false);
                            String text = statusEditPaket ? "Berhasil mengubah data paket"
                                    : "Gagal mengubah data paket !";
                            DetailSnack.show(this, text, 2, 0f);
                            if (statusEditPaket) {
                                load();
                            }
                        }
                    });
        }
    }
}
