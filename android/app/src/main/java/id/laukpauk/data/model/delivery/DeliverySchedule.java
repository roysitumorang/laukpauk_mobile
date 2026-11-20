package id.laukpauk.data.model.delivery;

import java.util.Map;

import androidx.annotation.Nullable;


import id.laukpauk.util.Json;

/**
 * One available delivery schedule (JadwalDelivery/getAvailableJadwalDelivery). The server object is kept
 * whole in {@link #raw} (dynamic payload): the legacy app stored it verbatim as {@code savedDeliveryScheduleMap}
 * (plus {@code setOn}) and the expiry check reads {@code id}, {@code day}, {@code maxTime}, {@code currentDate}.
 */
public final class DeliverySchedule {
    /** Key of the slot's start time (RFC 3339) in {@link #raw} for the options of {@code GET buyer/delivery_plans/now}. */
    public static final String KEY_DELIVERY_TIME = "delivery_time";

    public final Map<String, Object> raw;
    @com.squareup.moshi.Json(name = "kode")
    @Nullable public final String code;
    public final String day;
    @com.squareup.moshi.Json(name = "sessi")
    public final String session;
    @com.squareup.moshi.Json(name = "rentangWaktu")
    public final String timeRange;
    public final boolean isFull;
    public final boolean isOpened;

    public DeliverySchedule(Map<String, Object> raw) {
        this.raw = raw;
        this.code = Json.strOrNull(raw, "kode");
        this.day = Json.str(raw, "day");
        this.session = Json.str(raw, "sessi");
        this.timeRange = Json.str(raw, "rentangWaktu");
        this.isFull = Json.bool(raw, "isFull");
        this.isOpened = Json.bool(raw, "isOpened");
    }

    /**
     * Identity used to mark the selected tile: the delivery time of a delivery plan option, otherwise "Besok Pagi"
     * style (legacy readAbleJadwalPengantaran).
     */
    public String readable() {
        return code != null && raw.containsKey(KEY_DELIVERY_TIME) ? code : day + " " + session;
    }

    /** Legacy: tappable only when neither full nor closed. */
    public boolean isSelectable() {
        return !isFull && isOpened;
    }
}
