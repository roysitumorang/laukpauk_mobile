package id.laukpauk.data.model.payment;

import com.squareup.moshi.Json;

/** A payment method offered by GET Xendit/getPaymentChannels. */
public class PaymentChannel {
    public String channel_code;
    public String name;
    @Json(name = "minimalPembayaran")
    public long minimumPayment;
}
