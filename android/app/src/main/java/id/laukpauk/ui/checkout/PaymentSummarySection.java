package id.laukpauk.ui.checkout;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.core.content.ContextCompat;

import id.laukpauk.R;
import id.laukpauk.data.model.checkout.Voucher;
import id.laukpauk.databinding.ViewCheckoutSummaryBinding;
import id.laukpauk.ui.common.Images;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;

/** The payment summary: voucher, points, promo code and the totals (legacy RingkasanPembayaran). */
final class PaymentSummarySection {

    interface Listener {
        void onChooseVoucher();

        void onCancelVoucher();

        void onPointUsed(boolean used);

        void onPromoCode(String code);
    }

    private final ViewCheckoutSummaryBinding b;
    private final Context ctx;

    PaymentSummarySection(ViewCheckoutSummaryBinding binding, Listener listener) {
        this.b = binding;
        this.ctx = binding.getRoot().getContext();
        Images.asset(b.ivVoucher, "icons/pembeli/voucher.png");
        b.tvUseVoucher.setOnClickListener(v -> listener.onChooseVoucher());
        b.tvCancelVoucher.setOnClickListener(v -> listener.onCancelVoucher());
        b.cbPoint.setOnClickListener(v -> listener.onPointUsed(b.cbPoint.isChecked()));
        b.etPromoCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                listener.onPromoCode(s.toString());
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
        b.tvTotalPriceLabel.setText(ctx.getString(R.string.checkout_total_price, String.valueOf(s.goodsCount)));
        b.tvTotalPrice.setText(rp(s.subTotal));

        // voucher
        Voucher v = s.usedVoucher;
        String code = v != null && v.code != null ? v.code : "";
        String name = v != null && v.name != null ? v.name : "";
        String type = v != null && v.type != null ? v.type : "";
        show(b.tvVoucherInfo, v != null);
        show(b.tvCancelVoucher, v != null);
        b.tvVoucherInfo.setText(code + "  " + name);
        b.tvUseVoucher.setText(code.length() >= 1 && name.length() >= 1
                ? R.string.checkout_change : R.string.checkout_use_voucher);

        // points
        boolean minimumFulfilled = s.point >= s.minimumPointRedeem;
        long pointOutValue = s.pointOutValue >= s.subTotal ? s.subTotal : s.pointOutValue;
        long absPoint = pointOutValue <= -1 ? pointOutValue * -1 : pointOutValue;
        b.cbPoint.setChecked(s.isPointUsed);
        b.cbPoint.setEnabled(minimumFulfilled);
        show(b.tvPointMinimal, !minimumFulfilled);
        b.tvPointMinimal.setText(ctx.getString(R.string.checkout_minimum_points, rp(s.minimumPointRedeem)));
        b.tvPointValue.setText(rp(s.point));
        boolean partiallyUsed = s.isPointUsed && pointOutValue < s.point;
        show(b.tvPointUsed, partiallyUsed);
        b.tvPointUsed.setText(ctx.getString(R.string.checkout_points_used, rp(absPoint)));

        // promo message
        if (s.promoCodeMessage != null) {
            b.tvPromoMessage.setVisibility(View.VISIBLE);
            b.tvPromoMessage.setText(s.promoCodeMessage);
            int color = R.color.text_muted;
            if (s.promoCodeStatus != null) {
                color = s.promoCodeStatus ? R.color.text_success : R.color.text_danger;
            }
            b.tvPromoMessage.setTextColor(ContextCompat.getColor(ctx, color));
        } else {
            b.tvPromoMessage.setVisibility(View.GONE);
        }

        // rows
        boolean walletRow = s.walletDiscount > 0;
        show(b.rowWallet, walletRow);
        b.tvPotWallet.setText(minus(s.walletDiscount));
        show(b.rowPoints, s.isPointUsed);
        b.tvPointsDeduction.setText(minus(pointOutValue));

        boolean shippingFeeVoucher = type.toLowerCase().equals("ongkir");
        show(b.rowNormalShippingFee, shippingFeeVoucher);
        b.tvNormalShippingFee.setText(rp(s.totalShippingFee));

        show(b.rowVoucher, v != null);
        b.tvVoucherDeduction.setText(minus(s.voucherDiscount));
        b.tvVoucherDeductionRemark.setText(ctx.getString(R.string.checkout_voucher_discount_remark, type,
                s.statusVoucher == null ? "" : s.statusVoucher.toUpperCase()));

        long dk = s.categoryDiscount != null ? Json.longVal(s.categoryDiscount, "diskon") : 0;
        show(b.rowDiscount, s.categoryDiscount != null && dk >= 1);
        b.tvDiscount.setText(minus(dk));

        long shippingFeeShown = shippingFeeVoucher ? s.totalShippingFee - s.voucherDiscount : s.totalShippingFee;
        b.tvShippingFee.setText(rp(shippingFeeShown));
        boolean explanation = s.shippingFeeExplanation != null && !s.shippingFeeExplanation.isEmpty();
        show(b.tvExplanation, explanation);
        b.tvExplanation.setText(s.shippingFeeExplanation);
        b.tvGrandTotal.setText(rp(s.overallTotal));
    }
}
