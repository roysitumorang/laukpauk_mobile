package id.laukpauk.view.pembeli.components.listproduk;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Currency;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.pembeli.DetailProdukActivity;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.pembeli.components.homepage.HomeUi;

/** Port of lib/views/pembeli/components/ListProduk/ListProdukItem.dart (a product card in the ListProduk grid). */
public class ListProdukItem extends FrameLayout {
    private final JsonObject dataProduk;
    private final Runnable deteksiADJD;
    private final Double widgetWidth;
    private final FrameLayout priceHolder;

    public ListProdukItem(final Context ctx, JsonObject dataProduk, Runnable deteksiADJD, Double widgetWidth) {
        super(ctx);
        this.dataProduk = dataProduk;
        this.deteksiADJD = deteksiADJD;
        this.widgetWidth = widgetWidth;
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);

        final String idPenjual = Json.str(dataProduk, "idPenjual");
        String fotoProduk = Json.str(dataProduk, "foto");
        String namaProduk = Json.str(dataProduk, "nama");
        final String idProduk = Json.str(dataProduk, "id");
        String namaToko = Json.str(dataProduk, "namaToko");

        boolean isDefaultImage = fotoProduk.equals(DefaultImage.defaultProduk);

        // Card(margin: EdgeInsets.only(bottom: 10, left: 2.5, right: 2.5), radius 10)
        Ui.pad(this, 2.5f, 0, 2.5f, 10);
        LinearLayout card = Ui.column(ctx);
        Ui.background(card, CustomColors.white, 10);
        card.setElevation(Ui.dp(ctx, 1));
        Ui.clipRounded(card, 10);
        addView(card, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        FrameLayout imageBox = new FrameLayout(ctx);
        imageBox.addView(HomeUi.netImage(ctx, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk
                + "/compress/" + fotoProduk,
                isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP));
        LinearLayout.LayoutParams imageLp = Ui.weightH(1f);
        imageLp.bottomMargin = Ui.dp(ctx, 15);
        card.addView(imageBox, imageLp);

        LinearLayout texts = Ui.column(ctx);
        texts.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView nama = Ui.text(ctx, namaProduk, hb * 3.5f, 0xDD000000, true);
        nama.setEllipsize(android.text.TextUtils.TruncateAt.END);
        nama.setGravity(Gravity.CENTER);
        Ui.padXY(nama, 15, 0);
        texts.addView(nama, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        texts.addView(Ui.spaceH(ctx, 2));

        TextView toko = Ui.text(ctx, namaToko.length() >= 1 ? namaToko : "Penjual Tidak Diketahui",
                hb * 2.75f, CustomColors.textMuted);
        toko.setGravity(Gravity.CENTER);
        Ui.padXY(toko, 15, 0);
        Ui.onTap(toko, () -> Nav.push(ctx, PenjualActivity.class, Json.of("id", idPenjual)));
        texts.addView(toko, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        texts.addView(Ui.spaceH(ctx, 7.5f));

        priceHolder = new FrameLayout(ctx);
        Ui.padXY(priceHolder, 15, 0);
        priceHolder.addView(DetailUi.smallSpinner(ctx, 15), new FrameLayout.LayoutParams(Ui.MATCH, Ui.dp(ctx, 15)));
        texts.addView(priceHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        ScrollView scroll = new ScrollView(ctx);
        scroll.addView(texts, new android.view.ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(scroll, Ui.weightH(1f));

        Ui.onTap(this, () -> Nav.push(ctx, DetailProdukActivity.class,
                Json.of("idProduk", idProduk, "id", idProduk)));

        act.async(() -> ProdukModel.getLowestPrice(idProduk), this::showPrice);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widgetWidth != null && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(Ui.dp(getContext(), widgetWidth.floatValue()),
                    MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void showPrice(JsonElement detailPaket) {
        final Context ctx = getContext();
        final BaseActivity act = HomeUi.act(ctx);
        float hb = Ui.hb(ctx);
        priceHolder.removeAllViews();

        if (detailPaket == null || Json.isFalse(detailPaket) || !Json.isObject(detailPaket)) {
            TextView t = Ui.text(ctx, "Produk ini tidak memiliki daftar harga", hb * 2.7f, CustomColors.textMuted);
            t.setGravity(Gravity.CENTER);
            priceHolder.addView(t, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            return;
        }

        JsonObject paket = detailPaket.getAsJsonObject();
        int hargaBersih = Json.parseInt(Json.str(paket, "hargaBersih"), 0);
        String hargaStr = Json.strOrNull(paket, "harga");
        int hargaKotor = (hargaStr != null) ? Json.parseInt(hargaStr, 0) : 0;
        String diskonStr = Json.strOrNull(paket, "diskon");
        int diskon = (diskonStr != null) ? Json.parseInt(diskonStr, 0) : 0;
        double persentaseDiskon = (hargaKotor >= 1 && diskon >= 1) ? (double) diskon / hargaKotor * 100 : 0;

        LinearLayout column = Ui.column(ctx);
        column.setGravity(Gravity.CENTER_HORIZONTAL);

        if (persentaseDiskon > 0) {
            LinearLayout row = Ui.row(ctx);
            row.addView(HomeUi.t(ctx, Currency.formatRupiah(hargaKotor, 0), hb * 3));
            TextView chip = Ui.text(ctx, "-" + Currency.formatSatuan((long) persentaseDiskon, "%"),
                    hb * 2.75f, 0xDD000000);
            Ui.padXY(chip, 10, 2.5f);
            Ui.background(chip, 0xFFFFC107, 50);
            LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
            chipLp.leftMargin = Ui.dp(ctx, 5);
            row.addView(chip, chipLp);
            column.addView(row, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        }

        TextView harga = Ui.text(ctx, Currency.formatRupiah(hargaBersih, 0), hb * 3.25f, CustomColors.green, true);
        LinearLayout.LayoutParams hargaLp = new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP);
        hargaLp.topMargin = Ui.dp(ctx, 5);
        column.addView(harga, hargaLp);

        column.addView(Ui.spaceH(ctx, 10));

        View beli = CustomButton.button(ctx, "Beli", () -> act.async(
                () -> LocalStorage.isAlamatDeliveryDanJadwalDeliverySet(), (Boolean isADJDSet) -> {
                    if (Boolean.TRUE.equals(isADJDSet)) {
                        ProdukModel.showSatuanProduk(act, dataProduk, null);
                    } else {
                        ProdukModel.adjdNotSetAlert(act, (Boolean lakukanDeteksiADJD) -> {
                            if (lakukanDeteksiADJD != null && lakukanDeteksiADJD && deteksiADJD != null) {
                                deteksiADJD.run();
                            }
                        });
                    }
                }), new CustomButton.Opts().color(CustomButton.themeColor).size(CustomButton.smSize).pill().block());
        LinearLayout beliHolder = Ui.column(ctx);
        Ui.padXY(beliHolder, 10, 0);
        beliHolder.addView(beli, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        column.addView(beliHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        priceHolder.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }
}
