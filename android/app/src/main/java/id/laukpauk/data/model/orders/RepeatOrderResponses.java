package id.laukpauk.data.model.orders;

import java.util.List;

/** Response envelopes of the repeat-order endpoints. */
public final class RepeatOrderResponses {
    private RepeatOrderResponses() {}

    /** The re-orderable lines of a transaction (RO/getItemTransaksi). */
    public static class Items {
        /** false when the lines can't be delivered to the chosen address / schedule. */
        public boolean isAvailable;
        public List<ItemTransaksi> itemTransaksi;
    }

    public static class AddToCart {
        /** The key the legacy screen reads. */
        public boolean statusAddCart;
        public String message;
    }
}
