package id.laukpauk.data.model.checkout;

import com.squareup.moshi.Json;

/** The saved delivery schedule (prefs "savedDeliveryScheduleMap"). */
public class ShippingSchedule {
    @Json(name = "kode")
    public String code;
    public String day;
    @Json(name = "sessi")
    public String session;
    @Json(name = "rentangWaktu")
    public String timeRange;
    public String setOn;
    /** The plan's delivery time, exactly as {@code GET buyer/delivery_plans/now} sent it. */
    @Json(name = "delivery_time")
    public String deliveryTime;
}
