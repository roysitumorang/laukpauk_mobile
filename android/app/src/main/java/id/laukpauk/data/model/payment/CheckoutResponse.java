package id.laukpauk.data.model.payment;


/** The answer of POST Transaksi/checkout (legacy "responseCheckout"). */
public class CheckoutResponse {
    public boolean statusCheckout;
    public String message;
    public String idTransaksi;
    public String metodePembayaran;
    /** Dynamic: its shape depends on the payment channel (bank account, checkout_url, actions, message...). */
    public Object paymentResponse;

    public static CheckoutResponse failed() {
        CheckoutResponse r = new CheckoutResponse();
        r.statusCheckout = false;
        r.message = "Nande";
        return r;
    }
}
