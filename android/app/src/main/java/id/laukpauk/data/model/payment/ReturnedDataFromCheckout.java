package id.laukpauk.data.model.payment;

import java.util.Map;

/** Navigation argument of the order-success screens (JSON text): {responseCheckout, dataCheckout}. */
public class ReturnedDataFromCheckout {
    public CheckoutResponse responseCheckout;
    public Map<String, String> dataCheckout;
}
