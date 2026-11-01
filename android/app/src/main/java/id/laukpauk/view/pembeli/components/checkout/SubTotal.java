package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Currency;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.CheckoutModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomAlert;
import id.laukpauk.ui.CustomButton;
import id.laukpauk.ui.Modal;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/Checkout/SubTotal.dart: the TOTAL + "Bayar" bar at the top of the
 * Checkout screen. The Dart widget is stateless and is rebuilt with a fresh CheckoutGlobalState; here the
 * parent calls {@link #update(CheckoutGlobalState)}.
 */
public class SubTotal extends LinearLayout {
    private final Context checkoutContext;
    private CheckoutGlobalState cgs;

    /** 0 = not requested, 1 = loading, 2 = loaded (FutureBuilder of getWalletConfiguration). */
    private int walletState = 0;
    private JsonElement walletConfiguration;

    public SubTotal(Context ctx, Context checkoutContext, CheckoutGlobalState checkoutGlobalState) {
        super(ctx);
        this.checkoutContext = checkoutContext;
        this.cgs = checkoutGlobalState;
        setOrientation(VERTICAL);
        GradientDrawable bg = Ui.shape(ctx, CustomColors.nearlyWhite, 0);
        float r = Ui.dp(ctx, 12.5f);
        bg.setCornerRadii(new float[]{0, 0, 0, 0, r, r, r, r});
        setBackground(bg);
        setElevation(Ui.dp(ctx, 8)); // BoxShadow(0x22222222, offset 0,4.5, blur 50, spread 50)
        Ui.padAll(this, 20);
        render();
    }

    public void update(CheckoutGlobalState checkoutGlobalState) {
        this.cgs = checkoutGlobalState;
        render();
    }

    private void render() {
        final Context ctx = getContext();
        removeAllViews();
        float hb = Ui.hb(ctx);
        final CheckoutGlobalState c = cgs;

        LinearLayout row = Ui.row(ctx);
        row.setGravity(Gravity.TOP);
        LinearLayout left = Ui.column(ctx);
        left.addView(CheckoutUi.text(ctx, "TOTAL", hb * 5, 0xFF000000, false));
        left.addView(CheckoutUi.text(ctx, Currency.formatRupiah(c.totalKeseluruhan, 0), hb * 4,
                CustomColors.green, true));
        row.addView(left, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        final CustomButton.Btn bayar = CustomButton.button(ctx, "Bayar", this::onBayar,
                new CustomButton.Opts().color(CustomButton.themeColor).pill());
        bayar.setAlpha(c.isCheckoutFormCompleted ? 1f : .35f);
        row.addView(bayar, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        addView(row, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        if (c.isWalletOn) {
            if (walletState == 0) {
                walletState = 1;
                final BaseActivity a = CheckoutUi.activity(ctx);
                if (a != null) {
                    a.async(() -> CheckoutModel.getWalletConfiguration(), (JsonElement w) -> {
                        walletConfiguration = w;
                        walletState = 2;
                        render();
                    });
                }
            }
            if (walletState == 2) {
                if (walletConfiguration != null && walletConfiguration.isJsonObject()
                        && !Json.isFalse(walletConfiguration)) {
                    JsonObject w = walletConfiguration.getAsJsonObject();
                    long walletDiscount = Json.longVal(w, "walletDiscount");
                    long minGrand = Json.longVal(w, "minGrandTotalTransactionForWallet");
                    TextView t = CheckoutUi.text(ctx, "Potongan langsung dari dompet sebesar "
                            + Currency.formatRupiah(walletDiscount, 0) + " untuk setiap transaksi minimal "
                            + Currency.formatRupiah(minGrand, 0), hb * 3.25f, CheckoutUi.TEXT, false);
                    t.setTypeface(t.getTypeface(), android.graphics.Typeface.ITALIC);
                    t.setGravity(Gravity.CENTER);
                    addView(t, CheckoutUi.lp(Ui.MATCH, Ui.WRAP, 0, Ui.dp(ctx, 15), 0, 0));
                } else {
                    addView(CheckoutUi.plain(ctx, "Gagal mengambil data konfigurasi Dompet/Wallet"),
                            CheckoutUi.lp(Ui.MATCH, Ui.WRAP, 0, Ui.dp(ctx, 15), 0, 0));
                }
            } else {
                addView(CheckoutUi.spinner(ctx, 15, 0), new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
            }
        }
    }

    private void onBayar() {
        final BaseActivity a = CheckoutUi.activity(checkoutContext);
        if (a == null) {
            return;
        }
        final CheckoutGlobalState c = cgs;
        if (c.isCheckoutFormCompleted) {
            if (c.jadwalDelivery == null) {
                return; // Dart: `_jadwalDeliverySelected!` throws and nothing happens
            }
            JsonObject savedADJD = LocalStorage.getSavedLatitudeAndLongitude();
            String latDelivery = Json.str(savedADJD, "latitude");
            String longDelivery = Json.str(savedADJD, "longitude");

            String usedVoucherID = (c.usedVoucher == null) ? "0" : Json.str(c.usedVoucher, "id");

            JsonObject checkoutData = new JsonObject();
            checkoutData.addProperty("namaPenerima", Json.str(c.detailUser, "nama"));
            checkoutData.addProperty("nomorHPPenerima", Json.str(c.detailUser, "nomorHP"));
            checkoutData.addProperty("alamatDetailLengkap", Json.str(c.detailUser, "alamat"));
            checkoutData.addProperty("catatanTambahan", c.catatanTambahan);
            checkoutData.addProperty("jadwalPengiriman", Json.str(c.jadwalDelivery, "day") + " | "
                    + Json.str(c.jadwalDelivery, "rentangWaktu"));
            checkoutData.addProperty("cartCode", c.cartCode);
            checkoutData.addProperty("voucher", usedVoucherID);
            checkoutData.addProperty("latDelivery", latDelivery);
            checkoutData.addProperty("longDelivery", longDelivery);
            checkoutData.addProperty("jadwalDeliveryCode", Json.str(c.jadwalDelivery, "kode"));
            checkoutData.addProperty("isPointUsed", c.isPointUsed ? "true" : "false");
            checkoutData.addProperty("diskonKategori",
                    c.hasDiskonKategori() ? Json.toJson(c.diskonKategori) : "");
            checkoutData.addProperty("kodePromo", c.kodePromo);

            CheckoutModel.showMetodePembayaran(a, checkoutData, (int) c.totalKeseluruhan, null);
        } else {
            Modal.Content<Object> actions = handle -> CustomAlert.actionsRow(a,
                    CheckoutUi.textButton(a, "OK", 0xFF0069D9, handle::close));
            CustomAlert.<Object>textComponent(a, null, "Checkout",
                    "Anda harus melengkapi terlebih dahulu data - data yang diperlukan", actions, null,
                    false, null);
        }
    }
}
