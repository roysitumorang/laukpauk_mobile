package id.laukpauk.view.pembeli.components.detailproduk;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/DetailProduk/ShowSatuanProduk.dart (the content of the
 * "Pilihan Satuan" bottom sheet opened by ProdukModel.showSatuanProduk).
 */
public class ShowSatuanProduk extends FrameLayout {
    private final String idProduk;
    private final Async.Done<JsonObject> addCartCallback;

    private JsonObject satuanAndQuantitySelectedState;
    private final FrameLayout beliHolder;

    public ShowSatuanProduk(Context ctx, String idProduk, boolean isPenjualAvailable,
                            boolean isThisProductInCAVariable, JsonObject dataProduk,
                            String ongkirPerKMString, Async.Done<JsonObject> addCartCallback) {
        super(ctx);
        this.idProduk = idProduk;
        this.addCartCallback = addCartCallback;
        final float hb = Ui.hb(ctx);
        JsonObject data = dataProduk != null ? dataProduk : new JsonObject();

        String fotoProdukURL = ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/"
                + Json.str(data, "foto");

        String namaKategoriProduk = Json.has(data, "namaKategoriProduk")
                ? Json.str(data, "namaKategoriProduk") : "Kategori Produk Tidak Diketahui";

        boolean isStockAvailable = true;
        if (data.has("stokAvailable")) {
            isStockAvailable = "1".equals(Json.str(data, "stokAvailable"));
        }

        ScrollView scroll = new ScrollView(ctx);
        addView(scroll, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        LinearLayout column = Ui.column(ctx);
        Ui.pad(column, hb * 3, hb * 5, hb * 3, hb * 5);
        scroll.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        TextView title = Ui.text(ctx, "Pilihan Satuan", hb * 4, 0xDD000000, true);
        Ui.padXY(title, 7.5f, 0);
        column.addView(title, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        TextView sub = Ui.text(ctx, "Silahkan pilih satuan dan quantity yang anda mau : ", hb * 3, 0xDD000000);
        Ui.padXY(sub, 7.5f, 0);
        column.addView(sub, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(ctx, 10));

        LinearLayout row = Ui.row(ctx);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout info = Ui.row(ctx);
        info.setGravity(Gravity.CENTER_VERTICAL);
        Ui.padXY(info, 7.5f, 0);
        ImageView foto = new ImageView(ctx);
        foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Ui.loadNetworkCircle(foto, fotoProdukURL);
        info.addView(foto, new LinearLayout.LayoutParams(Ui.dp(ctx, hb * 15), Ui.dp(ctx, hb * 15)));
        info.addView(Ui.spaceW(ctx, 15));
        LinearLayout names = Ui.column(ctx);
        names.addView(Ui.text(ctx, Json.str(data, "nama"), new Txt.Style(hb * 4).bold().color(CustomColors.green)));
        names.addView(Ui.spaceH(ctx, 3));
        names.addView(Ui.text(ctx, namaKategoriProduk, hb * 3, CustomColors.textMuted));
        info.addView(names, Ui.weightW(1f));
        row.addView(info, Ui.weightW(1f));

        beliHolder = new FrameLayout(ctx);
        row.addView(beliHolder, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        column.addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(Ui.spaceH(ctx, 5));

        if (!isThisProductInCAVariable) {
            LinearLayout box = Ui.column(ctx);
            Ui.padXY(box, 15, 10);
            box.setBackground(Ui.shape(ctx, 0x55DC3545, 10f));
            box.addView(Ui.text(ctx, "Note!", new Txt.Style(hb * 3.5f).color(CustomColors.textDanger)));
            box.addView(Ui.spaceH(ctx, 5));
            box.addView(Ui.text(ctx, "Produk ini berada diluar Coverage Area Anda. Anda akan dikenakan ongkir "
                    + ongkirPerKMString + "/KM. Disarankan belanja dari 1 pasar di terdekat lokasi Anda",
                    Txt.textXS(ctx).italic()));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            lp.topMargin = Ui.dp(ctx, 5);
            lp.bottomMargin = Ui.dp(ctx, 5);
            column.addView(box, lp);
        }
        if (!isPenjualAvailable) {
            LinearLayout box = Ui.column(ctx);
            Ui.padXY(box, 15, 10);
            box.setBackground(Ui.shape(ctx, 0x55FFC107, 10f));
            box.addView(Ui.text(ctx, "Pemberitahuan", new Txt.Style(hb * 3.5f).color(CustomColors.nearlyBlack)));
            box.addView(Ui.spaceH(ctx, 5));
            box.addView(Ui.text(ctx, "Penjual dari produk ini tidak tersedia dalam jam pengantaran yang anda pilih!",
                    Txt.textXS(ctx).italic()));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
            lp.topMargin = Ui.dp(ctx, 5);
            lp.bottomMargin = Ui.dp(ctx, 5);
            column.addView(box, lp);
        }
        if (isPenjualAvailable) {
            column.addView(Ui.spaceH(ctx, 5));
            if (isStockAvailable) {
                column.addView(new PilihanSatuanProduk(ctx, idProduk, satuanAndQuantitySelected -> {
                    satuanAndQuantitySelectedState = satuanAndQuantitySelected;
                    updateBeliButton();
                }), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            } else {
                TextView habis = Ui.text(ctx, "Stok tidak tersedia", hb * 2.75f, CustomColors.textDanger);
                Ui.pad(habis, 0, 5, 0, 0);
                column.addView(habis, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            }
        }
    }

    /** `(_satuanAndQuantitySelectedState != null) ? CustomButton.button('Beli') : SizedBox()`. */
    private void updateBeliButton() {
        Context ctx = getContext();
        beliHolder.removeAllViews();
        if (satuanAndQuantitySelectedState != null) {
            beliHolder.addView(CustomButton.button(ctx, "Beli", this::onBeli,
                    new CustomButton.Opts().pill().color(CustomButton.themeColor).size(CustomButton.smSize)));
        }
    }

    private void onBeli() {
        JsonObject selected = satuanAndQuantitySelectedState;
        if (selected == null) {
            return;
        }
        String idSatuan = Json.str(selected, "idSatuan");
        String quantity = Json.str(selected, "quantity");
        String idPromo = null;
        if (selected.has("isPromo")) {
            idSatuan = Json.str(selected, "id");
            idPromo = Json.strOrNull(selected, "idPromo");
        }
        final String satuan = idSatuan;
        final String qty = quantity;
        final String promo = idPromo;
        ((BaseActivity) getContext()).async(
                () -> ProdukModel.addToCart(idProduk, satuan, qty, promo),
                response -> {
                    if (addCartCallback != null) {
                        addCartCallback.run(response);
                    }
                });
    }
}
