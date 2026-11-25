package id.laukpauk.ui.checkout;

import java.util.Map;

import androidx.annotation.Nullable;


import id.laukpauk.data.model.voucher.Voucher;

/** The computed totals of the Checkout screen (legacy CheckoutGlobalState + dataRingkasanPembayaran). Immutable. */
public final class CheckoutSummary {
    public boolean isFormCompleted;
    public boolean isPointUsed;
    /** {@code voucher_enabled} of {@code GET buyer/setting}: the voucher picker shows only then. */
    public boolean voucherEnabled;
    public long goodsCount;
    /** Basket total before wallet / voucher / shipping. */
    public long subTotal;
    public long totalShippingFee;
    public long walletDiscount;
    public long voucherDiscount;
    public long discountAmount;
    public long pointOutValue;
    public long point;
    public long minimumPointRedeem;
    public long overallTotal;
    public String shippingFeeExplanation = "";
    @Nullable public Voucher usedVoucher;
    @Nullable public String statusVoucher;
    @Nullable public Map<String, Object> categoryDiscount;
    @Nullable public Boolean promoCodeStatus;
    @Nullable public String promoCodeMessage;
}
