package id.laukpauk.ui.checkout;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.core.content.ContextCompat;

import id.laukpauk.R;
import id.laukpauk.data.model.checkout.Voucher;
import id.laukpauk.databinding.ViewCheckoutRingkasanBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;

/** The payment summary: voucher, points, promo code and the totals (legacy RingkasanPembayaran). */
final class RingkasanPembayaranSection {

    interface Listener {
        void onPilihVoucher();

        void onBatalVoucher();

        void onPointUsed(boolean used);

        void onKodePromo(String kode);
    }

    private final ViewCheckoutRingkasanBinding b;
    private final Context ctx;

    RingkasanPembayaranSection(ViewCheckoutRingkasanBinding binding, Listener listener) {
        this.b = binding;
        this.ctx = binding.getRoot().getContext();
        Images.asset(b.ivVoucher, "icons/pembeli/voucher.png");
        b.tvGunakanVoucher.setOnClickListener(v -> listener.onPilihVoucher());
        b.tvBatalVoucher.setOnClickListener(v -> listener.onBatalVoucher());
        b.cbPoint.setOnClickListener(v -> listener.onPointUsed(b.cbPoint.isChecked()));
        b.etKodePromo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                listener.onKodePromo(s.toString());
            }
        });
    }

    private String rp(long v) {
        return Currency.formatRupiah(v, 0);
    }

    private String minus(long v) {
        return ctx.getString(R.string.checkout_minus, rp(v));
    }

    private static void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    void render(CheckoutSummary s) {
        b.tvTotalHargaLabel.setText(ctx.getString(R.string.checkout_total_harga, String.valueOf(s.jumlahBarang)));
        b.tvTotalHarga.setText(rp(s.subTotal));

        // voucher
        Voucher v = s.usedVoucher;
        String kode = v != null && v.kode != null ? v.kode : "";
        String nama = v != null && v.nama != null ? v.nama : "";
        String jenis = v != null && v.jenis != null ? v.jenis : "";
        show(b.tvVoucherInfo, v != null);
        show(b.tvBatalVoucher, v != null);
        b.tvVoucherInfo.setText(kode + "  " + nama);
        b.tvGunakanVoucher.setText(kode.length() >= 1 && nama.length() >= 1
                ? R.string.checkout_ganti : R.string.checkout_gunakan_voucher);

        // points
        boolean minimalTerpenuhi = s.point >= s.minimalPointTukarPoint;
        long nilaiPointOut = s.nilaiPointOut >= s.subTotal ? s.subTotal : s.nilaiPointOut;
        long absPoint = nilaiPointOut <= -1 ? nilaiPointOut * -1 : nilaiPointOut;
        b.cbPoint.setChecked(s.isPointUsed);
        b.cbPoint.setEnabled(minimalTerpenuhi);
        show(b.tvPointMinimal, !minimalTerpenuhi);
        b.tvPointMinimal.setText(ctx.getString(R.string.checkout_minimal_poin, rp(s.minimalPointTukarPoint)));
        b.tvPointValue.setText(rp(s.point));
        boolean terpakaiSebagian = s.isPointUsed && nilaiPointOut < s.point;
        show(b.tvPointTerpakai, terpakaiSebagian);
        b.tvPointTerpakai.setText(ctx.getString(R.string.checkout_poin_terpakai, rp(absPoint)));

        // promo message
        if (s.kodePromoMessage != null) {
            b.tvPromoMessage.setVisibility(View.VISIBLE);
            b.tvPromoMessage.setText(s.kodePromoMessage);
            int color = R.color.text_muted;
            if (s.kodePromoStatus != null) {
                color = s.kodePromoStatus ? R.color.text_success : R.color.text_danger;
            }
            b.tvPromoMessage.setTextColor(ContextCompat.getColor(ctx, color));
        } else {
            b.tvPromoMessage.setVisibility(View.GONE);
        }

        // rows
        boolean walletRow = s.walletConfiguration != null && s.isWalletOn;
        show(b.rowWallet, walletRow);
        b.tvPotWallet.setText(minus(s.potonganWallet));
        show(b.rowPoin, s.isPointUsed);
        b.tvPotPoin.setText(minus(nilaiPointOut));

        boolean ongkirVoucher = jenis.toLowerCase().equals("ongkir");
        show(b.rowOngkirNormal, ongkirVoucher);
        b.tvOngkirNormal.setText(rp(s.totalOngkir));

        show(b.rowVoucher, v != null);
        b.tvPotVoucher.setText(minus(s.potonganVoucher));
        b.tvPotVoucherKet.setText(ctx.getString(R.string.checkout_potongan_voucher_ket, jenis,
                s.statusVoucher == null ? "" : s.statusVoucher.toUpperCase()));

        long dk = s.diskonKategori != null ? Json.longVal(s.diskonKategori, "diskon") : 0;
        show(b.rowDiskon, s.diskonKategori != null && dk >= 1);
        b.tvDiskon.setText(minus(dk));

        long ongkirShown = ongkirVoucher ? s.totalOngkir - s.potonganVoucher : s.totalOngkir;
        b.tvOngkir.setText(rp(ongkirShown));
        boolean penjelasan = s.jenisOngkir != null && s.penjelasanOngkir != null && !s.penjelasanOngkir.isEmpty();
        show(b.tvPenjelasan, penjelasan);
        b.tvPenjelasan.setText(s.penjelasanOngkir);
        b.tvGrandTotal.setText(rp(s.totalKeseluruhan));
    }
}
