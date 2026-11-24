package id.laukpauk.data.model.address;

import androidx.annotation.Nullable;

import java.util.List;

/** Envelopes of the {@code buyer/delivery_addresses} endpoints (the payload is in {@code data}). */
public final class AddressResponses {
    private AddressResponses() {}

    /** {@code GET buyer/delivery_addresses}: a page of addresses. */
    public static class AddressList {
        @Nullable public Page data;
    }

    public static class Page {
        @Nullable public Links links;
        @Nullable public List<BuyerAddress> rows;
    }

    public static class Links {
        /** Full URL of the next page; empty on the last page. */
        @Nullable public String next;
    }

    /** {@code POST buyer/delivery_addresses} and {@code PUT buyer/delivery_addresses/:id}: the saved address. */
    public static class Saved {
        @Nullable public BuyerAddress data;
    }
}
