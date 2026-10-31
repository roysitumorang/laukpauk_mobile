package id.laukpauk.view.pembeli.components.produkdidalampasar;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Currency;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.ProdukModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Layouts;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Txt;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.PenjualActivity;
import id.laukpauk.view.pembeli.DetailProdukActivity;

/**
 * Port of lib/views/pembeli/components/ProdukDiDalamPasar/ProdukDiDalamPasarItem.dart (the Dart
 * class is called ProdukInThisPasarItem; {@link ProdukInThisPasarItem} is an alias with the same
 * constructor).
 *
 * Meant to fill a grid cell of fixed size (Layouts.grid with an aspect ratio, as in Dart): the
 * image and the text part each take half of the cell. `widgetWidth` (dp, nullable) only applies
 * when the parent does not force the width (Flutter: the Container's width is ignored under tight
 * constraints).
 */
public class ProdukDiDalamPasarItem extends LinearLayout {
    private final JsonObject dataProduk;
    private final Runnable deteksiADJD;
    private final Double widgetWidth;

    private JsonElement lowestPriceState; // null = still loading
    private CustomButton.Btn beliButton;
    private boolean isButtonBeliBusy = false;

    public ProdukDiDalamPasarItem(Context ctx, JsonObject dataProduk, Runnable deteksiADJD,
                                  Double widgetWidth) {
        super(ctx);
        setOrientation(VERTICAL);
        this.dataProduk = dataProduk != null ? dataProduk : new JsonObject();
        this.deteksiADJD = deteksiADJD;
        this.widgetWidth = widgetWidth;
        render();
        initialization();
    }

    public ProdukDiDalamPasarItem(Context ctx, JsonObject dataProduk, Runnable deteksiADJD) {
        this(ctx, dataProduk, deteksiADJD, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widgetWidth != null && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(Ui.dp(getContext(), widgetWidth.floatValue()),
                    MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initialization() {
        final String idProduk = Json.str(dataProduk, "id");
        Async.Work<JsonElement> work = () -> ProdukModel.getLowestPrice(idProduk);
        Async.Done<JsonElement> done = lowestPrice -> {
            lowestPriceState = lowestPrice;
            render();
        };
        if (getContext() instanceof BaseActivity) {
            ((BaseActivity) getContext()).async(work, done);
        } else {
            Async.run(work, done);
        }
    }

    private boolean isLoaded() {
        return lowestPriceState != null && !Json.isNull(lowestPriceState);
    }

    private boolean hasPrice() {
        return isLoaded() && !Json.isFalse(lowestPriceState);
    }

    private void render() {
        final Context ctx = getContext();
        final float hb = Ui.hb(ctx);
        removeAllViews();
        beliButton = null;
        Ui.pad(this, 0, 5, 0, 5); // Container margin: top 5, bottom 5

        final String namaProduk = Json.str(dataProduk, "nama");
        final String idPenjual = Json.str(dataProduk, "idPenjual");
        final String fotoProduk = Json.str(dataProduk, "foto");
        final String idProduk = Json.str(dataProduk, "id");
        String namaToko = Json.has(dataProduk, "namaToko") ? Json.str(dataProduk, "namaToko") : "-";

        boolean isStockAvailable = true;
        if (dataProduk.has("stokAvailable")) {
            isStockAvailable = "1".equals(Json.str(dataProduk, "stokAvailable"));
        }

        boolean isDefaultImage = fotoProduk.equals(DefaultImage.defaultProduk);

        double persentaseDiskon = 0;
        long hargaKotor = 0;
        long hargaBersih = 0;
        long diskon = 0;

        if (hasPrice()) {
            JsonObject lp = Json.obj(lowestPriceState);
            diskon = Json.parseInt(Json.str(lp, "diskon"), 0);
            hargaKotor = Json.parseInt(Json.str(lp, "harga"), 0);
            hargaBersih = Json.parseInt(Json.str(lp, "hargaBersih"), 0);

            persentaseDiskon = hargaKotor == 0 ? 0 : (double) diskon / hargaKotor * 100;
        }

        boolean isInActivePromo = false;
        if (dataProduk.has("promo")) {
            JsonArray promo = Json.array(dataProduk, "promo");
            isInActivePromo = promo.size() >= 1;
        }

        String deskripsiProduk = "";
        if (dataProduk.has("keteranganSingkat")) {
            deskripsiProduk = Json.str(dataProduk, "keteranganSingkat");
        }

        LinearLayout card = Layouts.card(ctx, CustomColors.nearlyWhite, 10f, 2f);
        Layouts.ripple(card, 0x33000000, 10f);
        card.setOnClickListener(v -> Nav.push(ctx, DetailProdukActivity.class, Json.of("idProduk", idProduk)));
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f);
        cardLp.leftMargin = Ui.dp(ctx, 2.5f);
        cardLp.rightMargin = Ui.dp(ctx, 2.5f);
        addView(card, cardLp);

        // ---- image part (Expanded)
        FrameLayout imageArea = new FrameLayout(ctx);
        ImageView image = new ImageView(ctx);
        image.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
        Ui.loadNetwork(image, ServerPath.baseURL + "/" + ServerPath.uploadGambarProduk + "/compress/" + fotoProduk);
        imageArea.addView(image, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));
        Ui.clipRounded(imageArea, 10f);
        card.addView(imageArea, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));

        if (persentaseDiskon > 0) {
            TextView hemat = Ui.text(ctx, "Hemat " + Currency.formatSatuan((long) Math.ceil(persentaseDiskon), "%"),
                    hb * 2.75f, 0xDD000000);
            Ui.padXY(hemat, 10, 2.5f);
            hemat.setBackground(Ui.shape(ctx, 0xFFFFC107, 50f));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.TOP | Gravity.END);
            lp.topMargin = Ui.dp(ctx, 5);
            lp.rightMargin = Ui.dp(ctx, 5);
            lp.leftMargin = Ui.dp(ctx, 5);
            imageArea.addView(hemat, lp);
        }
        if (isInActivePromo) {
            TextView promo = Ui.text(ctx, "Promo", hb * 2.75f, CustomColors.nearlyWhite);
            Ui.padXY(promo, 10, 2.5f);
            promo.setBackground(Ui.shape(ctx, CustomColors.textDanger, 50f));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.TOP | Gravity.START);
            lp.topMargin = Ui.dp(ctx, 5);
            lp.leftMargin = Ui.dp(ctx, 5);
            imageArea.addView(promo, lp);
        }

        // ---- text part (Expanded)
        if (isLoaded()) {
            ScrollView scroll = new ScrollView(ctx);
            scroll.setFillViewport(true);
            scroll.setVerticalScrollBarEnabled(false);
            LinearLayout column = Ui.column(ctx);
            column.setGravity(Gravity.CENTER_VERTICAL);
            Ui.pad(column, 7.5f, 10, 7.5f, 0);
            scroll.addView(column, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            TextView nama = Ui.text(ctx, namaProduk, hb * 3.5f, 0xDD000000, true);
            nama.setGravity(Gravity.CENTER);
            nama.setEllipsize(android.text.TextUtils.TruncateAt.END);
            column.addView(nama, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            column.addView(Ui.spaceH(ctx, 2));

            TextView toko = Ui.text(ctx, namaToko.length() >= 1 ? namaToko : "Penjual Tidak Diketahui",
                    hb * 2.75f, CustomColors.textMuted);
            toko.setSingleLine(true);
            View tokoScroll = Layouts.hscroll(ctx, toko);
            LinearLayout tokoHolder = Ui.column(ctx);
            Ui.pad(tokoHolder, 0, 0, 0, 7.5f);
            tokoHolder.addView(tokoScroll, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            Layouts.ripple(tokoHolder, 0x33000000, 0f);
            tokoHolder.setOnClickListener(v -> Nav.push(ctx, PenjualActivity.class, Json.of("id", idPenjual)));
            toko.setOnClickListener(v -> Nav.push(ctx, PenjualActivity.class, Json.of("id", idPenjual)));
            column.addView(tokoHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

            if (!deskripsiProduk.isEmpty()) {
                TextView desk = Ui.text(ctx, deskripsiProduk, hb * 2.75f, CustomColors.textMuted);
                desk.setMaxLines(2);
                LinearLayout deskHolder = Ui.column(ctx);
                Ui.pad(deskHolder, 0, 0, 0, 7.5f);
                deskHolder.addView(Layouts.hscroll(ctx, desk), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                column.addView(deskHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }

            LinearLayout priceRow = Ui.row(ctx);
            priceRow.setGravity(Gravity.CENTER_VERTICAL);
            priceRow.addView(Ui.text(ctx, Currency.formatRupiah(hargaBersih, 0),
                    Txt.hargaBersih(ctx).size(hb * 2.5f)));
            if (persentaseDiskon > 0) {
                TextView coret = Ui.text(ctx, Currency.formatRupiah(hargaKotor, 0),
                        Txt.hargaCoret(ctx).size(hb * 2.5f).color(CustomColors.nearlyBlack));
                Ui.pad(coret, 5, 0, 0, 0);
                priceRow.addView(coret);
            }
            LinearLayout priceHolder = Ui.column(ctx);
            Ui.pad(priceHolder, 0, 0, 0, 3.5f);
            priceHolder.addView(Layouts.hscroll(ctx, priceRow), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            column.addView(priceHolder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            column.addView(Ui.spaceH(ctx, 3.5f));

            if (isStockAvailable) {
                if (hasPrice()) {
                    beliButton = CustomButton.button(ctx, "Beli", this::onBeli,
                            new CustomButton.Opts().color(CustomButton.themeColor).size(CustomButton.smSize)
                                    .pill().busy(isButtonBeliBusy));
                    LinearLayout holder = Ui.column(ctx);
                    holder.setGravity(Gravity.CENTER_HORIZONTAL);
                    Ui.pad(holder, 0, 5, 0, 10);
                    holder.addView(beliButton);
                    column.addView(holder, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                } else {
                    TextView none = Ui.text(ctx, "Tidak ada daftar harga",
                            Txt.textXS(ctx).size(hb * 2.75f));
                    none.setGravity(Gravity.CENTER);
                    column.addView(none, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
                }
            } else {
                TextView stok = Ui.text(ctx, "Stok tidak tersedia", hb * 2.75f, CustomColors.textDanger);
                stok.setGravity(Gravity.CENTER);
                column.addView(stok, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
            Ui.pad(column, 7.5f, 10, 7.5f, 0);
            card.addView(scroll, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));
        } else {
            card.addView(Loading.view(ctx, null, null, 30f, 30f), new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));
        }

        if (!deskripsiProduk.isEmpty()) {
            card.addView(Ui.spaceH(ctx, 15));
        }
    }

    private void setBusy(boolean busy) {
        isButtonBeliBusy = busy;
        if (beliButton != null) {
            beliButton.setBusy(busy);
        }
    }

    private void onBeli() {
        final BaseActivity activity = (BaseActivity) getContext();
        setBusy(true);
        boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
        if (isADJDSet) {
            ProdukModel.showSatuanProduk(activity, dataProduk, v -> setBusy(false));
        } else {
            ProdukModel.adjdNotSetAlert(activity, lakukanDeteksiADJD -> {
                if (lakukanDeteksiADJD != null && lakukanDeteksiADJD) {
                    if (deteksiADJD != null) {
                        deteksiADJD.run();
                    }
                }
                setBusy(false);
            });
        }
    }
}
