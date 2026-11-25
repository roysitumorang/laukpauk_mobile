package id.laukpauk.data.model.voucher;

import androidx.annotation.Nullable;

import java.util.List;

/** Response wrappers of the voucher endpoints. */
public final class VoucherResponses {
    private VoucherResponses() {}

    /** {@code GET buyer/vouchers}: the backend envelope; the vouchers are in {@code data}. */
    public static final class BuyerVouchers {
        @Nullable public List<Voucher> data;
    }
}
