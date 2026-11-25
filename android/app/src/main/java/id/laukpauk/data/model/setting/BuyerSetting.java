package id.laukpauk.data.model.setting;

import com.squareup.moshi.Json;

/** The store settings the buyer app uses ({@code GET buyer/setting}). */
public class BuyerSetting {
    /** Vouchers can be used at checkout. */
    @Json(name = "voucher_enabled")
    public boolean voucherEnabled;
    /** Rupiah per kilometre; the delivery cost is {@code distance_km} of the address times this. */
    @Json(name = "delivery_cost_per_km")
    public long deliveryCostPerKm;
    /** Farthest delivery distance in km (the backend refuses addresses beyond it). */
    @Json(name = "delivery_max_distance_km")
    public long deliveryMaxDistanceKm;
}
