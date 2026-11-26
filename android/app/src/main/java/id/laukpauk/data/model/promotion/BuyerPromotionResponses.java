package id.laukpauk.data.model.promotion;

import androidx.annotation.Nullable;

/** Envelope of {@code GET buyer/promotions/:promo_code}. */
public final class BuyerPromotionResponses {
    private BuyerPromotionResponses() {}

    /** The promotion in {@code data}. */
    public static class One {
        @Nullable public BuyerPromotion data;
    }
}
