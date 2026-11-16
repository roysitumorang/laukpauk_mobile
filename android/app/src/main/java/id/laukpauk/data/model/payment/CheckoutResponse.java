package id.laukpauk.data.model.payment;

import com.squareup.moshi.Json;


/** The answer of POST Transaksi/checkout (legacy "responseCheckout"). */
public class CheckoutResponse {
    public boolean statusCheckout;
    public String message;
    @Json(name = "idTransaksi")
    public String transactionId;
    @Json(name = "metodePembayaran")
    public String paymentMethod;
    /** Dynamic: its shape depends on the payment channel (bank account, checkout_url, actions, message...). */
    public Object paymentResponse;

    public static CheckoutResponse failed() {
        CheckoutResponse r = new CheckoutResponse();
        r.statusCheckout = false;
        r.message = "Nande";
        return r;
    }
}
