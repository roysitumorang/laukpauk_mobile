package id.laukpauk.data.model.delivery;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * The delivery plan of the current transaction window ({@code GET buyer/delivery_plans/now}, {@code data}). Only the
 * keys the app uses are declared.
 */
public class DeliveryPlan {
    @Nullable public String id;
    @Nullable public String name;
    /** Go duration of one delivery slot, e.g. "1h0m0s" (0s = a point in time). */
    @Json(name = "delivery_time_interval")
    @Nullable public String deliveryTimeInterval;
    /** The slot start times (RFC 3339), the options; checkout must send one of them exactly. */
    @Json(name = "delivery_times")
    @Nullable public List<String> deliveryTimes;
}
