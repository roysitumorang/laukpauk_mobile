package id.laukpauk.ui.checkout;


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
    public long pointOutValue;
    public long point;
    public long minimumPointRedeem;
    public long overallTotal;
    public String shippingFeeExplanation = "";
    @Nullable public Voucher usedVoucher;
    @Nullable public String statusVoucher;
    /** The promo code's discount off the shopping cost (0 when there is no valid code). */
    public long promoCodeDiscount;
    /** The promo code field: empty, being checked, valid or not valid. */
    public static final int PROMO_NONE = 0;
    public static final int PROMO_CHECKING = 1;
    public static final int PROMO_VALID = 2;
    public static final int PROMO_INVALID = 3;
    public int promoCodeState = PROMO_NONE;
    /** The backend's message when the check failed for another reason than "not valid" (404); else null. */
    @Nullable public String promoCodeMessage;
}
