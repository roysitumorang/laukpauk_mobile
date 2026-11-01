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

import com.google.gson.JsonObject;

import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.DefaultImage;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.core.ServerPath;
import id.laukpauk.model.PaketModel;
import id.laukpauk.core.AppState;
import id.laukpauk.model.HeaderModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Nav;
import id.laukpauk.ui.Ui;
import id.laukpauk.view.pembeli.DetailPaketActivity;
import id.laukpauk.view.pembeli.DetailUi;
import id.laukpauk.view.penjual.PenjualUi;
import id.laukpauk.view.penjual.TambahProdukActivity;

/** Port of lib/views/components/Penjual/PaketPenjualCard.dart. */
public class PaketPenjualCard extends LinearLayout {
    private final JsonObject dataProduk;
    private final Runnable deteksiADJD;
    private final Runnable reload;

    private Integer currentUserIDState;

    public PaketPenjualCard(Context ctx, JsonObject dataProduk, Runnable deteksiADJD, Runnable reload) {
        super(ctx);
        this.dataProduk = dataProduk;
        this.deteksiADJD = deteksiADJD;
        this.reload = reload;
        setOrientation(VERTICAL);
        initialization();
        render();
    }

    private BaseActivity activity() {
        return (BaseActivity) getContext();
    }

    private void initialization() {
        String currentUserIDString = LocalStorage.getCurrentUserID();
        try {
            currentUserIDState = Integer.parseInt(currentUserIDString.trim());
        } catch (NumberFormatException e) {
            currentUserIDState = null; // Dart: int.parse throws, the state stays null
        }
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
        float hb = Ui.hb(ctx);

        JsonObject dataPaket = dataProduk;

        int idPenjual = parseInt(Json.str(dataPaket, "idPenjual"));
        final boolean produkPenjual = currentUserIDState != null && currentUserIDState == idPenjual;

        final String idPaket = Json.str(dataPaket, "id");
        int hargaKotor = parseInt(Json.str(dataPaket, "harga"));

        int diskon = parseInt(Json.str(dataPaket, "diskon"));
        int hargaBersih = hargaKotor - diskon;
        double persentaseDiskon = hargaKotor == 0 ? 0 : (double) diskon / hargaKotor * 100;
        String fotoPaket = Json.str(dataPaket, "foto");

        boolean isDefaultImage = fotoPaket.equals(DefaultImage.defaultPaket);

        // Container(padding: top 5) > InkWell > Card(margin: bottom 10, left 2.5, right 2.5)
        Ui.pad(this, 0, 5, 0, 0);
        setGravity(Gravity.TOP);

        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(VERTICAL);
        card.setBackground(Ui.shape(ctx, CustomColors.white, 10f));
        card.setElevation(Ui.dp(ctx, 1));
        LinearLayout.LayoutParams cardLp = PenjualUi.lp(ctx, Ui.MATCH, 0, 2.5f, 0, 2.5f, 10);
        cardLp.weight = 1f;
        addView(card, cardLp);
        Ui.onTap(card, () -> Nav.push(ctx, DetailPaketActivity.class, Json.of("idPaket", idPaket)));

        // Expanded: image
        ImageView image = new ImageView(ctx);
        Ui.loadNetwork(image, ServerPath.baseURL + "/" + ServerPath.uploadGambarPaket + "/compress/" + fotoPaket);
        image.setScaleType(isDefaultImage ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.FIT_XY);
        DetailUi.clipTopRounded(image, 10f);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f);
        imgLp.bottomMargin = Ui.dp(ctx, 10);
        card.addView(image, imgLp);

        // Expanded: details
        ScrollView scroll = new ScrollView(ctx);
        LinearLayout col = Ui.column(ctx);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        Ui.pad(col, 5, 0, 5, 0);
        scroll.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        card.addView(scroll, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));

        TextView nama = PenjualUi.text(ctx, Json.str(dataPaket, "nama"), 3.5f, 0xDD000000, true);
        nama.setGravity(Gravity.CENTER);
        col.addView(nama, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(Ui.spaceH(ctx, 5));

        if (persentaseDiskon > 0) {
            LinearLayout row = Ui.row(ctx);
            row.addView(PenjualUi.text(ctx, Currency.formatRupiah(hargaKotor, 0), 3f, 0xDD000000, false));
            if (diskon >= 1) {
                TextView badge = PenjualUi.text(ctx, "-" + Currency.formatSatuan((long) persentaseDiskon, "%"), 3f,
                        0xDD000000, false);
                badge.setBackground(Ui.shape(ctx, PenjualUi.amber, 50f));
                Ui.padXY(badge, 10, 5);
                row.addView(badge, PenjualUi.lp(ctx, Ui.WRAP, Ui.WRAP, 5, 0, 0, 0));
            }
            HorizontalScrollView hs = new HorizontalScrollView(ctx);
            hs.setHorizontalScrollBarEnabled(false);
            hs.addView(row, new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
            col.addView(hs, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        }

        TextView harga = PenjualUi.text(ctx, Currency.formatRupiah(hargaBersih, 0), 3.5f, CustomColors.green, true);
        col.addView(harga, PenjualUi.lp(ctx, Ui.WRAP, Ui.WRAP, 0, 5, 0, 0));
        col.addView(Ui.spaceH(ctx, 7.5f));

        CustomButton.Opts opts = new CustomButton.Opts()
                .color(produkPenjual ? CustomButton.warningColor : CustomButton.themeColor)
                .block().pill().size(CustomButton.xsSize);
        View button = CustomButton.button(ctx, produkPenjual ? "Edit" : "Beli", () -> onButton(produkPenjual, idPaket),
                opts);
        col.addView(button, PenjualUi.lp(ctx, Ui.MATCH, Ui.WRAP, 10, 0, 10, 0));
    }

    private void onButton(boolean produkPenjual, final String idPaket) {
        final BaseActivity activity = activity();
        if (!produkPenjual) {
            boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
            if (isADJDSet) {
                activity.async(() -> PaketModel.addToCart(idPaket), statusAddToCart -> {
                    String text = (statusAddToCart != null && statusAddToCart)
                            ? "Produk berhasil ditambahkan ke keranjang !"
                            : "Produk gagal ditambahkan ke keranjang, silahkan coba lagi.";

                    PaketModel.addToCartSnackBar(activity, text);

                    activity.async(() -> HeaderModel.getJumlahCart(), jumlahKeranjang ->
                            AppState.get().setJumlahCart(jumlahKeranjang == null ? 0 : jumlahKeranjang));
                });
            } else {
                PaketModel.adjdNotSetAlert(activity, tentukanADJD -> {
                    if (tentukanADJD != null && tentukanADJD) {
                        deteksiADJD.run();
                    }
                });
            }
        } else {
            Nav.pushForResult(activity, TambahProdukActivity.class,
                    Json.of("idProduk", idPaket, "jenisProduk", "paket"), result -> {
                        if (result != null && result.isJsonPrimitive()) {
                            boolean statusEditPaket = Json.asBool(result, false);
                            String text = statusEditPaket ? "Berhasil mengubah data paket"
                                    : "Gagal mengubah data paket !";
                            PenjualUi.snackDefault(activity, text, 2);
                            reload.run();
                        }
                    });
        }
    }
}
