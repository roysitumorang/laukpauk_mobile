package id.laukpauk.data.model.orders;

import java.util.List;

/** Response envelopes of the repeat-order endpoints. */
public final class RepeatOrderResponses {
    private RepeatOrderResponses() {}

    public static class Items {
        public List<RoPenjual> itemTransaksi;
    }

    public static class AddToCart {
        /** The key the legacy screen reads. */
        public boolean statusAddCart;
        public String message;
    }
}
