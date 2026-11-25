package id.laukpauk.data.model.orders;

import androidx.annotation.Nullable;

import java.util.List;

/** Envelopes of the {@code buyer/transactions} endpoints (the payload is in {@code data}). */
public final class BuyerTransactionResponses {
    private BuyerTransactionResponses() {}

    /** {@code POST buyer/transactions} (201) and {@code GET buyer/transactions/:id}. */
    public static class One {
        @Nullable public BuyerTransaction data;
    }

    /** {@code GET buyer/transactions}: page-based ({@code data.links}, {@code data.info}), rows in {@code data.rows}. */
    public static class Page {
        @Nullable public PageData data;
    }

    public static class PageData {
        @Nullable public Links links;
        @Nullable public List<BuyerTransaction> rows;
    }

    public static class Links {
        /** Full URL of the next page; "" on the last page. */
        @Nullable public String next;
    }
}
