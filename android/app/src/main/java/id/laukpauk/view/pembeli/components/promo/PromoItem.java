package id.laukpauk.view.pembeli.components.promo;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonObject;

import java.util.Locale;

import id.laukpauk.core.AppState;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.model.ShopModelUi;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailPromoItemActivity;

/** Port of lib/views/pembeli/components/Promo/PromoItem.dart (a promo product card). */
public class PromoItem extends LinearLayout {

    private static final class CartResult {
        JsonObject response;
        int jumlahKeranjang;
    }

    /** Dart: PromoItem(dataProduk, {widgetWidth}). The grid cell dictates the width, as in Flutter. */
    public PromoItem(Context ctx, JsonObject dataProduk, Double widgetWidth) {
        super(ctx);
        setOrientation(VERTICAL);
        if (dataProduk == null) {
            return;
        }
        final BaseActivity activity = (BaseActivity) ctx;
        final float hb = Ui.hb(ctx);

        final String idPromo = Json.str(dataProduk, "idPromo");
        final String idPromoItem = Json.str(dataProduk, "idPromoItem");

        int diskonPromo = Json.parseInt(Json.str(dataProduk, "diskonPromo"), 0);
        int hargaKotor = Json.parseInt(Json.str(dataProduk, "hargaProduk"), 0);
        int diskon = Json.parseInt(Json.str(dataProduk, "diskonProduk"), 0);

        int hargaBersihPromo = hargaKotor - diskon - diskonPromo;
        double persentaseDiskon = 0;

        final String idProduk = Json.str(dataProduk, "idProduk");
        final String idSatuan = Json.str(dataProduk, "idHargaProduk");

        String fotoProduk = Json.str(dataProduk, "fotoProduk");
        boolean isDefaultImage = fotoProduk.equals(DefaultImage.defaultProduk);

        if (diskon >= 1 && hargaKotor >= 1) {
            persentaseDiskon = (double) diskon / hargaKotor * 100;
        }

        String maxPesan = Json.str(dataProduk, "maxOrder");
        String deskripsiProduk = Json.str(dataProduk, "keteranganSingkat", "");

        // InkWell > Card(margin: bottom 10, left/right 1.5) > Container
        Ui.pad(this, 1.5f, 0, 1.5f, 10);
        setClipChildren(false);
        setClipToPadding(false);

        LinearLayout card = Ui.column(ctx);
        card.setBackground(Ui.shape(ctx, CustomColors.nearlyWhite, 10));
        card.setElevation(Ui.dp(ctx, 1));
        Layouts.ripple(card, 0x1F000000, 10f);
        card.setOnClickListener(v -> Nav.push(activity, DetailPromoItemActivity.class,
                Json.of("idPromoItem", idPromoItem)));
        addView(card, new LayoutParams(Ui.MATCH, Ui.MATCH));

        // ---- top half: image + badges
        FrameLayout imageArea = new FrameLayout(ctx);
        card.addView(imageArea, Ui.weightH(1f));

        ImageView image = new ImageView(ctx);
        image.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
        Ui.loadNetwork(image, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/"
                + fotoProduk);
        FrameLayout.LayoutParams imageLp = new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH);
        imageLp.bottomMargin = Ui.dp(ctx, 10);
        imageArea.addView(image, imageLp);
        id.laukpauk.view.pembeli.DetailUi.clipTopRounded(image, 10f);

        if (persentaseDiskon >= 0.1) {
            TextView hemat = Ui.text(ctx,
                    "Hemat " + String.format(Locale.US, "%.1f", persentaseDiskon) + " %",
                    hb * 2.0f, 0xDD000000);
            Ui.padXY(hemat, 7.5f, 2.5f);
            hemat.setBackground(Ui.shape(ctx, CustomColors.textWarning, 50));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                    Gravity.TOP | Gravity.RIGHT);
            lp.topMargin = Ui.dp(ctx, 5);
            lp.rightMargin = Ui.dp(ctx, 5);
            imageArea.addView(hemat, lp);
        }

        TextView max = Ui.text(ctx, "Max " + maxPesan + "x pesan", hb * 2.0f, 0xDD000000);
        Ui.padXY(max, 7.5f, 2.5f);
        max.setBackground(Ui.shape(ctx, CustomColors.textWarning, 15));
        FrameLayout.LayoutParams maxLp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP,
                Gravity.BOTTOM | Gravity.LEFT);
        maxLp.leftMargin = Ui.dp(ctx, 5);
        maxLp.bottomMargin = Ui.dp(ctx, 20);
        imageArea.addView(max, maxLp);

        // ---- bottom half: texts + button
        LinearLayout bottomHolder = Ui.column(ctx);
        Ui.pad(bottomHolder, 0, 0, 0, 10);
        card.addView(bottomHolder, Ui.weightH(1f));
        ScrollView scroll = new ScrollView(ctx);
        scroll.setVerticalScrollBarEnabled(false);
        bottomHolder.addView(scroll, new LayoutParams(Ui.MATCH, Ui.MATCH));

        LinearLayout info = Ui.column(ctx);
        info.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(info, 5, 0, 5, 0);
        scroll.addView(info, new ViewGroupLp());

        TextView nama = Ui.text(ctx, Json.str(dataProduk, "namaProduk"), hb * 3.5f, 0xDD000000, true);
        nama.setEllipsize(android.text.TextUtils.TruncateAt.END);
        nama.setGravity(Gravity.CENTER);
        info.addView(nama, new LayoutParams(Ui.MATCH, Ui.WRAP));
        info.addView(Ui.spaceH(ctx, 2));

        String namaToko = Json.str(dataProduk, "namaToko");
        TextView toko = Ui.text(ctx, namaToko.length() >= 1 ? namaToko : "Penjual Tidak Diketahui",
                hb * 2.75f, 0xDD000000);
        toko.setGravity(Gravity.CENTER);
        toko.setHorizontallyScrolling(true);
        HorizontalScrollView tokoScroll = Layouts.hscroll(ctx, toko);
        LayoutParams tokoLp = new LayoutParams(Ui.WRAP, Ui.WRAP);
        tokoLp.gravity = Gravity.CENTER_HORIZONTAL;
        info.addView(tokoScroll, tokoLp);

        if (deskripsiProduk.length() >= 1) {
            TextView desc = Ui.text(ctx, deskripsiProduk,
                    Txt.textXS(ctx).color(CustomColors.textMuted).size(hb * 2.75f));
            desc.setMaxLines(2);
            desc.setHorizontallyScrolling(true);
            HorizontalScrollView descScroll = Layouts.hscroll(ctx, desc);
            LayoutParams descLp = new LayoutParams(Ui.WRAP, Ui.WRAP);
            descLp.gravity = Gravity.CENTER_HORIZONTAL;
            descLp.topMargin = Ui.dp(ctx, 5);
            descLp.leftMargin = Ui.dp(ctx, 5);
            descLp.rightMargin = Ui.dp(ctx, 5);
            info.addView(descScroll, descLp);
        }
        info.addView(Ui.spaceH(ctx, 5));

        LinearLayout harga = Ui.column(ctx);
        harga.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.margin(harga, 0, 2.5f, 0, 3.5f);
        harga.addView(Ui.text(ctx, Currency.formatRupiah(hargaBersihPromo, 0),
                Txt.hargaBersih(ctx).size(hb * 2.5f)));
        harga.addView(Ui.spaceH(ctx, 3.5f));
        harga.addView(Ui.text(ctx, Currency.formatRupiah(hargaKotor, 0),
                Txt.hargaCoret(ctx).size(hb * 2.5f).color(CustomColors.nearlyBlack)));
        LayoutParams hargaLp = new LayoutParams(Ui.WRAP, Ui.WRAP);
        hargaLp.gravity = Gravity.CENTER_HORIZONTAL;
        hargaLp.topMargin = Ui.dp(ctx, 2.5f);
        hargaLp.bottomMargin = Ui.dp(ctx, 3.5f);
        info.addView(harga, hargaLp);

        final CustomButton.Btn beli = CustomButton.button(ctx, "Beli", () -> {
            activity.async(() -> {
                CartResult r = new CartResult();
                r.response = ProdukModel.addToCart(idProduk, idSatuan, "1", idPromo);
                if (r.response != null) {
                    r.jumlahKeranjang = HeaderModel.getJumlahCart();
                }
                return r;
            }, r -> {
                if (r == null || r.response == null) {
                    return;
                }
                JsonObject response = r.response;
                boolean statusAddCart = Json.bool(response, "statusAddCart");
                if (statusAddCart) {
                    ShopModelUi.keranjangSnackbar(activity,
                            "Berhasil ditambahkan ke dalam keranjang !", 3);
                    AppState.get().setJumlahCart(r.jumlahKeranjang);
                } else {
                    String messageCart =
                            "Maaf mak, produk gagal ditambahkan ke dalam keranjang. Coba lagi yah...";
                    if (Json.has(response, "message") || response.has("message")) {
                        messageCart = Json.str(response, "message");
                    }
                    CustomAlert.<Object>textComponent(activity, null, "Keranjang", messageCart,
                            handle -> CustomAlert.actionsRow(activity,
                                    CustomButton.button(activity, "OK", () -> handle.close(),
                                            new CustomButton.Opts().color(CustomColors.green))),
                            null, false, null);
                }
            });
        }, new CustomButton.Opts().size(CustomButton.xsSize).color(CustomButton.themeColor).pill());
        LayoutParams beliLp = new LayoutParams(Ui.MATCH, Ui.WRAP);
        beliLp.topMargin = Ui.dp(ctx, 5);
        beliLp.leftMargin = Ui.dp(ctx, 10);
        beliLp.rightMargin = Ui.dp(ctx, 10);
        info.addView(beli, beliLp);
    }

    private static final class ViewGroupLp extends ScrollView.LayoutParams {
        ViewGroupLp() {
            super(Ui.MATCH, Ui.WRAP);
        }
    }
}
