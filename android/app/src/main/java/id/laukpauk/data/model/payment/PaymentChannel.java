package id.laukpauk.data.model.payment;

import androidx.annotation.Nullable;

/** A payment channel of {@code GET buyer/payment_channels} ({@code data[]}). */
public class PaymentChannel {
    @Nullable public String id;
    /** e.g. "BCA Virtual Account". */
    @Nullable public String name;
    /** e.g. "BCA_VIRTUAL_ACCOUNT"; also picks the logo in assets/img/payment/. */
    @Nullable public String channel_code;
}
