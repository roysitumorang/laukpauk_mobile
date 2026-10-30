package id.laukpauk.view.components.penjual;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailProdukActivity;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.TambahProdukActivity;

/** Port of lib/views/components/Penjual/ProdukPenjualCard.dart. */
public class ProdukPenjualCard extends LinearLayout {
    private final JsonObject dataProduk;
    private final Runnable deteksiADJD;
    private final Runnable reload;

    private Integer currentUserIDState;

    public ProdukPenjualCard(Context ctx, JsonObject dataProduk, Runnable deteksiADJD, Runnable reload) {
        super(ctx);
        this.dataProduk = dataProduk;
        this.deteksiADJD = deteksiADJD;
        this.reload = reload;
        setOrientation(VERTICAL);
        try {
            currentUserIDState = Integer.parseInt(LocalStorage.getCurrentUserID().trim());
        } catch (NumberFormatException e) {
            currentUserIDState = null; // Dart: int.parse throws, the state stays null
        }
        render();
    }

    private BaseActivity activity() {
        return (BaseActivity) getContext();
    }

    /** Dart getLowestPrice(): the `lowestPrice` entry, or null (= false) on failure. BLOCKING. */
    private JsonElement getLowestPrice() {
        String idProduk = Json.str(dataProduk, "id");

        Http.Response httpResponse = Http.getApi("/Produk/getLowestPriceOfThisProduct/" + idProduk);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return dataObject.get("lowestPrice");
        }
        return null;
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private void render() {
        removeAllViews();
        final Context ctx = getContext();

        String namaKategoriProduk = !Json.isNull(Json.get(dataProduk, "namaKategoriProduk"))
                ? Json.str(dataProduk, "namaKategoriProduk")
                : "Kategori Tidak Diketahui";
        boolean kategoriNull = Json.isNull(Json.get(dataProduk, "namaKategoriProduk"));

        int idPenjual = parseInt(Json.str(dataProduk, "idPenjual"));
        final boolean produkSaya = currentUserIDState != null && currentUserIDState == idPenjual;
        String fotoProduk = Json.str(dataProduk, "foto");
        boolean isDefaultImage = fotoProduk.equals(DefaultImage.defaultProduk);

        Ui.pad(this, 0, 5, 0, 0);

        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(VERTICAL);
        card.setBackground(Ui.shape(ctx, CustomColors.white, 10f));
        card.setElevation(Ui.dp(ctx, 1));
        LinearLayout.LayoutParams cardLp = PenjualUi.lp(ctx, Ui.MATCH, 0, 2.5f, 0, 2.5f, 10);
        cardLp.weight = 1f;
        addView(card, cardLp);
        Ui.onTap(card, () -> Nav.push(ctx, DetailProdukActivity.class,
                Json.of("id", Json.str(dataProduk, "id"))));

        ImageView image = new ImageView(ctx);
        Ui.loadNetwork(image, ServerPath.apiBaseURL + "/" + ServerPath.uploadGambarProduk + "/compress/" + fotoProduk);
        image.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
        DetailUi.clipTopRounded(image, 10f);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f);
        imgLp.bottomMargin = Ui.dp(ctx, 10);
        card.addView(image, imgLp);

        ScrollView scroll = new ScrollView(ctx);
        final LinearLayout col = Ui.column(ctx);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(col, 5, 0, 5, 0);
        scroll.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(scroll, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));

        TextView nama = PenjualUi.text(ctx, Json.str(dataProduk, "nama"), 3.5f, 0xDD000000, true);
        nama.setGravity(Gravity.CENTER);
        col.addView(nama, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(Ui.spaceH(ctx, 2));

        TextView kategori = PenjualUi.text(ctx, namaKategoriProduk, 3f, 0xDD000000, false);
        kategori.setGravity(Gravity.CENTER);
        if (kategoriNull) {
            kategori.setTypeface(kategori.getTypeface(), android.graphics.Typeface.ITALIC);
        }
        col.addView(kategori, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (!produkSaya) {
            col.addView(Ui.spaceH(ctx, 5));
            final FrameLayout priceHolder = new FrameLayout(ctx);
            priceHolder.addView(PenjualUi.spinner(ctx, 15), new FrameLayout.LayoutParams(Ui.dp(ctx, 15),
                    Ui.dp(ctx, 15), Gravity.CENTER_HORIZONTAL));
            col.addView(priceHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            PenjualUi.async(ctx, () -> getLowestPrice(), detailHarga -> {
                priceHolder.removeAllViews();
                priceHolder.addView(buildPrice(detailHarga), new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            });
        } else {
            View edit = CustomButton.button(ctx, "Edit", () -> onEdit(),
                    new CustomButton.Opts().pill().color(CustomButton.warningColor).block()
                            .size(CustomButton.smSize));
            col.addView(edit, PenjualUi.lp(ctx, Ui.MATCH, Ui.WRAP, 10, 10, 10, 0));
        }
    }

    private View buildPrice(JsonElement detailHargaElement) {
        Context ctx = getContext();
        JsonObject detailHarga = Json.isFalse(detailHargaElement) || !Json.isObject(detailHargaElement)
                ? null : Json.obj(detailHargaElement);
        if (detailHarga == null) {
            TextView tv = PenjualUi.text(ctx, "Produk ini tidak memiliki daftar harga", 3f, CustomColors.textMuted,
                    false);
            tv.setGravity(Gravity.CENTER);
            return tv;
        }

        int hargaBersih = parseInt(Json.str(detailHarga, "hargaBersih"));
        int hargaKotor = parseInt(Json.str(detailHarga, "harga"));
        int diskon = parseInt(Json.str(detailHarga, "diskon"));
        double persentaseDiskon = (diskon >= 1 && hargaKotor >= 1) ? (double) diskon / hargaKotor * 100 : 0;

        LinearLayout column = Ui.column(ctx);
        column.setGravity(Gravity.CENTER_HORIZONTAL);

        if (persentaseDiskon > 0) {
            LinearLayout row = Ui.row(ctx);
            row.addView(PenjualUi.text(ctx, Currency.formatRupiah(hargaKotor, 0), 2.75f, 0xDD000000, false));
            TextView badge = PenjualUi.text(ctx, "-" + Currency.formatSatuan((long) persentaseDiskon, "%"), 2.75f,
                    0xDD000000, false);
            badge.setBackground(Ui.shape(ctx, PenjualUi.amber, 50f));
            Ui.padXY(badge, 10, 2.5f);
            row.addView(badge, PenjualUi.lp(ctx, Ui.WRAP, Ui.WRAP, 5, 0, 0, 0));
            HorizontalScrollView hs = new HorizontalScrollView(ctx);
            hs.setHorizontalScrollBarEnabled(false);
            hs.addView(row, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            column.addView(hs, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        column.addView(PenjualUi.text(ctx, Currency.formatRupiah(hargaBersih, 0), 3.25f, CustomColors.green, true),
                PenjualUi.lp(ctx, Ui.WRAP, Ui.WRAP, 0, 2.5f, 0, 0));
        column.addView(Ui.spaceH(ctx, 10));
        column.addView(CustomButton.button(ctx, "Beli", () -> onBeli(),
                new CustomButton.Opts().pill().color(CustomButton.themeColor).block().size(CustomButton.smSize)),
                PenjualUi.lp(ctx, Ui.MATCH, Ui.WRAP, 10, 0, 10, 0));
        return column;
    }

    private void onBeli() {
        final BaseActivity activity = activity();
        boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
        if (isADJDSet) {
            ProdukModel.showSatuanProduk(activity, dataProduk, null);
        } else {
            ProdukModel.adjdNotSetAlert(activity, lakukanDeteksiADJD -> {
                if (lakukanDeteksiADJD != null && lakukanDeteksiADJD) {
                    deteksiADJD.run();
                }
            });
        }
    }

    private void onEdit() {
        final BaseActivity activity = activity();
        Nav.pushForResult(activity, TambahProdukActivity.class,
                Json.of("idProduk", Json.str(dataProduk, "id"), "jenisProduk", "satuan"), result -> {
                    if (result != null && result.isJsonPrimitive()) {
                        boolean statusEditProduk = Json.asBool(result, false);
                        String text = statusEditProduk ? "Berhasil mengubah data produk"
                                : "Gagal mengubah data produk !";
                        PenjualUi.snackDefault(activity, text, 2);
                        reload.run();
                    }
                });
    }
}
