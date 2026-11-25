package id.laukpauk.data.model.payment;

import androidx.annotation.Nullable;

/** Envelopes of the {@code buyer/transactions} endpoints (the payload is in {@code data}). */
public final class TransactionResponses {
    private TransactionResponses() {}

    /** {@code POST buyer/transactions}: 201 with the new transaction. */
    public static class Created {
        @Nullable public BuyerTransaction data;
    }
}
