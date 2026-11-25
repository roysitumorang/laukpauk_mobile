package id.laukpauk.data.model.payment;

import androidx.annotation.Nullable;

import java.util.List;

/** {@code GET buyer/payment_channels}: the backend envelope; the channels are in {@code data} (null when none). */
public class PaymentChannelsResponse {
    @Nullable public List<PaymentChannel> data;
}
