package id.laukpauk.view.pembeli.components.checkout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;

import id.laukpauk.core.Async;
import id.laukpauk.core.Currency;
import id.laukpauk.core.CustomColors;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.model.CheckoutModel;
import id.laukpauk.ui.BaseActivity;
import id.laukpauk.ui.CustomForm;
import id.laukpauk.ui.Loading;
import id.laukpauk.ui.Ui;

/**
 * Port of lib/views/pembeli/components/Checkout/RingkasanPembayaran.dart. The Dart widget is rebuilt by its
 * parent on every state change; here the parent calls {@link #update(JsonObject, CheckoutGlobalState)}.
 * The promo-code text field (a State field in Dart) survives updates.
 */
public class RingkasanPembayaran extends LinearLayout {

    /** Dart: void Function(Map dataVoucher, String statusVoucher). */
    public interface VoucherSelected {
        void run(JsonObject dataVoucher, String statusVoucher);
    }

    private final CheckoutGlobalState[] cgsHolder = new CheckoutGlobalState[1];
    private JsonObject dataRingkasanPembayaran;
    private final VoucherSelected onVoucherSelected;
    private final Runnable onVoucherRemoved;
    private final Async.Done<Boolean> onPointUsed;
    private final Async.Done<String> onKodePromoFilled;

    private final EditText kodePromoController;
    private final CustomForm.TextFieldView kodePromoField;
    private final TextView kodePromoMessageView;
    private final LinearLayout promoRow;
    private final LinearLayout content;
    private final LinearLayout bottom;

    public RingkasanPembayaran(Context ctx, JsonObject dataRingkasanPembayaran,
                               CheckoutGlobalState checkoutGlobalState, VoucherSelected onVoucherSelected,
                               Runnable onVoucherRemoved, Async.Done<Boolean> onPointUsed,
                               Async.Done<String> onKodePromoFilled) {
        super(ctx);
        setOrientation(VERTICAL);
        this.dataRingkasanPembayaran = dataRingkasanPembayaran;
        this.cgsHolder[0] = checkoutGlobalState;
        this.onVoucherSelected = onVoucherSelected;
        this.onVoucherRemoved = onVoucherRemoved;
        this.onPointUsed = onPointUsed;
        this.onKodePromoFilled = onKodePromoFilled;

        kodePromoController = CustomForm.controller(ctx);
        CustomForm.TextFieldOpts o = new CustomForm.TextFieldOpts().borderWidth(1.0)
                .borderColor(CustomColors.green)
                .onChange(value -> {
                    if (this.onKodePromoFilled != null) {
                        this.onKodePromoFilled.run(value);
                    }
                });
        kodePromoField = CustomForm.textField(ctx, "Kode Promo", kodePromoController, o);
        LinearLayout kodePromoColumn = Ui.column(ctx);
        kodePromoColumn.setGravity(Gravity.END);
        kodePromoColumn.addView(kodePromoField, matchWrap());
        kodePromoMessageView = CheckoutUi.text(ctx, "", Ui.hb(ctx) * 3.0f, CustomColors.textMuted, false);
        kodePromoMessageView.setVisibility(View.GONE);
        kodePromoColumn.addView(kodePromoMessageView,
                CheckoutUi.lp(Ui.WRAP, Ui.WRAP, 0, Ui.dp(ctx, 7.5f), 0, 0));

        // the promo field is built once so that it keeps focus/keyboard while the parent updates
        promoRow = Ui.row(ctx);
        promoRow.setGravity(Gravity.TOP);
        promoRow.addView(CheckoutUi.plain(ctx, "Kode Promo"));
        promoRow.addView(Ui.spaceW(ctx, 15));
        promoRow.addView(kodePromoColumn, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        content = Ui.column(ctx);
        bottom = Ui.column(ctx);
        addView(content, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(promoRow, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        addView(bottom, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        render();
    }

    public void update(JsonObject dataRingkasanPembayaran, CheckoutGlobalState checkoutGlobalState) {
        this.dataRingkasanPembayaran = dataRingkasanPembayaran;
        this.cgsHolder[0] = checkoutGlobalState;
        render();
    }

    private TextView greyText(Context ctx, String s, float size, boolean bold) {
        return CheckoutUi.text(ctx, s, size, CustomColors.grey, bold);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
    }

    private void render() {
        final Context ctx = getContext();
        content.removeAllViews();
        bottom.removeAllViews();

        boolean isReadyToShow = Json.bool(dataRingkasanPembayaran, "isReadyToShow");
        promoRow.setVisibility(isReadyToShow ? View.VISIBLE : View.GONE);
        if (!isReadyToShow) {
            content.addView(Loading.view(ctx), matchWrap());
            return;
        }

        final float hb = Ui.hb(ctx);
        final CheckoutGlobalState cgs = cgsHolder[0];

        JsonObject usedVoucher = cgs.usedVoucher;
        JsonObject walletConfigurationState = Json.objectOrNull(dataRingkasanPembayaran,
                "walletConfigurationState");
        String statusVoucher = Json.strOrNull(dataRingkasanPembayaran, "statusVoucher");
        long jumlahBarang = Json.longVal(dataRingkasanPembayaran, "jumlahBarang");
        final long totalBelanja = cgs.subTotal;
        long totalOngkir = cgs.ongkir;
        long totalOngkirNormal = Json.longVal(dataRingkasanPembayaran, "totalOngkirNormal");
        JsonObject jenisOngkir = Json.objectOrNull(dataRingkasanPembayaran, "jenisOngkir");
        String penjelasanOngkir = Json.str(dataRingkasanPembayaran, "penjelasanOngkir");

        String jumlahBarangString = String.valueOf(jumlahBarang);

        boolean isSelectedVoucherNull = usedVoucher == null;
        boolean isWalletConfigurationNull = walletConfigurationState == null;

        String kodeVoucher = !isSelectedVoucherNull ? Json.str(usedVoucher, "kode") : "";
        String namaVoucher = !isSelectedVoucherNull ? Json.str(usedVoucher, "nama") : "";
        String jenisVoucher = !isSelectedVoucherNull ? Json.str(usedVoucher, "jenis") : "";

        long nilaiVoucher = 0;
        if (!isSelectedVoucherNull) {
            if (statusVoucher != null) {
                if (statusVoucher.toLowerCase().equals("aktif")) {
                    nilaiVoucher = Json.longVal(usedVoucher, "nilai");
                }
            }
        }

        if (jenisVoucher.toLowerCase().equals("ongkir")) {
            totalOngkir = totalOngkir - nilaiVoucher;
        }

        float iconWidth = hb * 5.75f;

        boolean isPointUsed = cgs.isPointUsed;
        long jumlahPoint = cgs.point;
        long totalKeseluruhan = cgs.totalKeseluruhan;
        long potonganWallet = cgs.potonganDompet;
        boolean isWalletOn = cgs.isWalletOn;
        long nilaiPointOut = cgs.nilaiPointOut;

        long dkDiskonKategori = 0;
        if (cgs.hasDiskonKategori()) {
            dkDiskonKategori = Json.longVal(cgs.diskonKategori.getAsJsonObject(), "diskon");
        }

        final boolean minimalPointTerpenuhi = jumlahPoint >= cgs.minimalPointTukarPoint;

        if (nilaiPointOut >= totalBelanja) {
            nilaiPointOut = totalBelanja;
        }

        long absoluteNilaiPointOut = nilaiPointOut;
        if (nilaiPointOut <= -1) {
            absoluteNilaiPointOut = nilaiPointOut * -1;
        }

        Boolean kodePromoStatus = cgs.kodePromoStatus;
        String kodePromoMessage = cgs.kodePromoMessage;

        int kodePromoMessageColor = CustomColors.textMuted;
        if (kodePromoStatus != null) {
            kodePromoMessageColor = CustomColors.textDanger;
            if (kodePromoStatus) {
                kodePromoMessageColor = CustomColors.textSuccess;
            }
        }

        content.addView(CheckoutUi.text(ctx, "Ringkasan Pembayaran", hb * 4, CheckoutUi.TEXT, true));
        content.addView(Ui.spaceH(ctx, 5));
        content.addView(CheckoutUi.spaceBetween(ctx,
                greyText(ctx, "Total Harga (" + jumlahBarangString + " Barang)", hb * 3.5f, false),
                greyText(ctx, Currency.formatRupiah(totalBelanja, 0), hb * 3.5f, false)), matchWrap());
        content.addView(Ui.spaceH(ctx, 20));

        // ---- voucher box
        LinearLayout voucherBox = Ui.row(ctx);
        voucherBox.setBackground(id.laukpauk.ui.Layouts.border(ctx, CustomColors.green, 1, 10));
        Ui.pad(voucherBox, 15, 10, 15, 10);
        voucherBox.addView(Ui.asset(ctx, "icons/pembeli/voucher.png", iconWidth));
        voucherBox.addView(Ui.spaceW(ctx, 10));
        LinearLayout voucherInfo = Ui.column(ctx);
        voucherInfo.addView(CheckoutUi.plain(ctx, "Voucher"));
        if (!isSelectedVoucherNull) {
            voucherInfo.addView(Ui.spaceH(ctx, 2.5f));
            LinearLayout kn = Ui.row(ctx);
            kn.addView(CheckoutUi.text(ctx, kodeVoucher, hb * 2.75f, CheckoutUi.TEXT, false));
            kn.addView(Ui.spaceW(ctx, 5));
            kn.addView(CheckoutUi.text(ctx, namaVoucher, hb * 2.75f, CheckoutUi.TEXT, false));
            voucherInfo.addView(kn);
            TextView batal = CheckoutUi.text(ctx, "Batal Pakai Voucher", hb * 3, CustomColors.textMuted, false);
            batal.setOnClickListener(v -> {
                if (onVoucherRemoved != null) {
                    onVoucherRemoved.run();
                }
            });
            voucherInfo.addView(batal, CheckoutUi.lp(Ui.WRAP, Ui.WRAP, 0, Ui.dp(ctx, 5), 0, 0));
        }
        voucherBox.addView(voucherInfo, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        final JsonObject usedVoucherFinal = usedVoucher;
        TextView gunakan = CheckoutUi.text(ctx,
                (kodeVoucher.length() >= 1 && namaVoucher.length() >= 1) ? "Ganti" : "Gunakan Voucher",
                hb * 3, CustomColors.textPrimary, false);
        gunakan.setOnClickListener(v -> pilihVoucher(totalBelanja, usedVoucherFinal));
        voucherBox.addView(gunakan);
        content.addView(voucherBox, matchWrap());
        content.addView(Ui.spaceH(ctx, 10));

        // ---- point box
        LinearLayout pointBox = Ui.row(ctx);
        pointBox.setBackground(id.laukpauk.ui.Layouts.border(ctx, CustomColors.green, 1, 10));
        Ui.pad(pointBox, 15, 10, 15, 10);
        CheckBox cb = new CheckBox(ctx);
        cb.setButtonTintList(ColorStateList.valueOf(CustomColors.green));
        cb.setChecked(isPointUsed);
        cb.setEnabled(minimalPointTerpenuhi);
        cb.setOnClickListener(v -> {
            if (minimalPointTerpenuhi && onPointUsed != null) {
                onPointUsed.run(((CheckBox) v).isChecked());
            }
        });
        pointBox.addView(cb, new LinearLayout.LayoutParams(Ui.dp(ctx, 15), Ui.dp(ctx, 15)));
        pointBox.addView(Ui.spaceW(ctx, 10));
        LinearLayout pointInfo = Ui.column(ctx);
        pointInfo.addView(CheckoutUi.plain(ctx, "Gunakan Poin"));
        if (!minimalPointTerpenuhi) {
            pointInfo.addView(CheckoutUi.text(ctx, "Minimal penukaran poin "
                    + Currency.formatRupiah(cgs.minimalPointTukarPoint, 0), hb * 2.5f, CheckoutUi.TEXT, false),
                    CheckoutUi.lp(Ui.WRAP, Ui.WRAP, 0, Ui.dp(ctx, 2.5f), 0, 0));
        }
        pointBox.addView(pointInfo, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        pointBox.addView(Ui.spaceW(ctx, 5));
        LinearLayout pointValue = Ui.column(ctx);
        pointValue.setGravity(Gravity.END);
        pointValue.addView(CheckoutUi.text(ctx, Currency.formatRupiah(jumlahPoint, 0), hb * 3.5f,
                CustomColors.green, true));
        if (isPointUsed && nilaiPointOut < jumlahPoint) {
            pointValue.addView(CheckoutUi.text(ctx, "Terpakai Hanya "
                    + Currency.formatRupiah(absoluteNilaiPointOut, 0), hb * 2.5f, CheckoutUi.TEXT, false));
        }
        pointBox.addView(pointValue);
        content.addView(pointBox, matchWrap());
        content.addView(Ui.spaceH(ctx, 10));

        // ---- kode promo (the row itself lives in promoRow)
        if (kodePromoMessage != null) {
            kodePromoMessageView.setText(kodePromoMessage);
            kodePromoMessageView.setTextColor(kodePromoMessageColor);
            kodePromoMessageView.setVisibility(View.VISIBLE);
        } else {
            kodePromoMessageView.setVisibility(View.GONE);
        }
        final LinearLayout content = this.bottom;
        content.addView(Ui.spaceH(ctx, 15));
        if (minimalPointTerpenuhi) {
            content.addView(Ui.spaceH(ctx, 10));
        }
        if (!isWalletConfigurationNull && isWalletOn) {
            content.addView(Ui.spaceH(ctx, 5));
            LinearLayout wallet = Ui.column(ctx);
            wallet.addView(CheckoutUi.spaceBetween(ctx,
                    greyText(ctx, "Potongan Dompet", hb * 3.5f, false),
                    greyText(ctx, "(" + Currency.formatRupiah(potonganWallet, 0) + ")", hb * 3.5f, false)),
                    CheckoutUi.lp(Ui.MATCH, Ui.WRAP, 0, Ui.dp(ctx, 5), 0, 0));
            if (isPointUsed) {
                wallet.addView(CheckoutUi.spaceBetween(ctx,
                        greyText(ctx, "Potongan Poin", hb * 3.5f, false),
                        greyText(ctx, "(" + Currency.formatRupiah(nilaiPointOut, 0) + ")", hb * 3.5f, false)),
                        matchWrap());
            }
            content.addView(wallet, matchWrap());
        }

        if (jenisVoucher.toLowerCase().equals("ongkir")) {
            content.addView(CheckoutUi.spaceBetween(ctx,
                    greyText(ctx, "Ongkir Normal ", hb * 3.5f, false),
                    greyText(ctx, Currency.formatRupiah(totalOngkirNormal, 0), hb * 3.5f, false)),
                    CheckoutUi.lp(Ui.MATCH, Ui.WRAP, 0, Ui.dp(ctx, 5), 0, 0));
        }

        if (!isSelectedVoucherNull) {
            LinearLayout nilai = Ui.column(ctx);
            nilai.setGravity(Gravity.END);
            nilai.addView(greyText(ctx, "(" + Currency.formatRupiah(nilaiVoucher, 0) + ")", hb * 3.5f, false));
            TextView ket = greyText(ctx, "Potongan " + jenisVoucher + " (Voucher "
                    + (statusVoucher == null ? "" : statusVoucher.toUpperCase()) + ")", hb * 2.5f, false);
            ket.setGravity(Gravity.END);
            nilai.addView(ket);
            content.addView(CheckoutUi.spaceBetween(ctx, greyText(ctx, "Potongan Voucher", hb * 3.5f, false),
                    nilai), CheckoutUi.lp(Ui.MATCH, Ui.WRAP, 0, Ui.dp(ctx, 5), 0, 0));
        }

        if (cgs.hasDiskonKategori() && dkDiskonKategori >= 1) {
            content.addView(CheckoutUi.spaceBetween(ctx, greyText(ctx, "Discount", hb * 3.5f, false),
                    greyText(ctx, "(" + Currency.formatRupiah(dkDiskonKategori, 0) + ")", hb * 3.5f, false)),
                    CheckoutUi.lp(Ui.MATCH, Ui.WRAP, 0, Ui.dp(ctx, 5), 0, 0));
        }

        content.addView(CheckoutUi.spaceBetween(ctx, greyText(ctx, "Total Ongkos Kirim", hb * 3.5f, false),
                greyText(ctx, Currency.formatRupiah(totalOngkir, 0), hb * 3.5f, false)),
                CheckoutUi.lp(Ui.MATCH, Ui.WRAP, 0, Ui.dp(ctx, 5), 0, 0));

        if (jenisOngkir != null) {
            TextView pj = CheckoutUi.text(ctx, penjelasanOngkir, hb * 2.75f, CustomColors.textDanger, false);
            pj.setTypeface(pj.getTypeface(), Typeface.ITALIC);
            content.addView(pj, CheckoutUi.lp(Ui.MATCH, Ui.WRAP, 0, Ui.dp(ctx, 10), 0, Ui.dp(ctx, 15)));
        }

        content.addView(Ui.divider(ctx, CustomColors.green, 1), matchWrap());
        content.addView(Ui.spaceH(ctx, 20));
        content.addView(CheckoutUi.spaceBetween(ctx,
                greyText(ctx, "Total Yang Harus Dibayar", hb * 3.5f, true),
                CheckoutUi.text(ctx, Currency.formatRupiah(totalKeseluruhan, 0), hb * 4.5f,
                        CustomColors.darkGreen, true)), matchWrap());
    }

    private void pilihVoucher(long totalBelanja, JsonObject usedVoucher) {
        final BaseActivity a = CheckoutUi.activity(getContext());
        if (a == null) {
            return;
        }
        CheckoutModel.showVoucher(a, (int) totalBelanja, usedVoucher, (JsonObject selectedVoucher) -> {
            final String idSelectedVoucher = Json.str(selectedVoucher, "id");
            a.async(() -> LocalStorage.getStatusVoucher(idSelectedVoucher), (String status) -> {
                LocalStorage.setUsedVoucher(idSelectedVoucher);
                if (onVoucherSelected != null) {
                    onVoucherSelected.run(selectedVoucher, status);
                }
            });
        }, null);
    }
}
