package id.laukpauk.data.model.promotion;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

/**
 * A promotion of {@code GET buyer/promotions/:promo_code}: the backend only returns it while it is active, within
 * its dates and under the buyer's quota; {@code discount} comes off the shopping cost.
 */
public class BuyerPromotion {
    @Nullable public String id;
    @Nullable public String name;
    @Nullable public String code;
    @Nullable public String description;
    /** Flat amount off the shopping cost. */
    public long discount;
    @Json(name = "quota_per_buyer")
    public long quotaPerBuyer;
    /** How many times this buyer has used it. */
    public long usages;
    @Json(name = "effective_start_date")
    @Nullable public String effectiveStartDate;
    @Json(name = "effective_end_date")
    @Nullable public String effectiveEndDate;
}
