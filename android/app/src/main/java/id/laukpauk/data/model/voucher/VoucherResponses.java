package id.laukpauk.data.model.voucher;

import java.util.List;

/** Response wrappers of the voucher endpoints. */
public final class VoucherResponses {
    private VoucherResponses() {}

    public static final class VoucherList {
        public List<Voucher> listVoucher;
    }

    /** dataVoucher is {@code false} when the voucher does not exist (null here). */
    public static final class Detail {
        public Voucher dataVoucher;
    }
}
