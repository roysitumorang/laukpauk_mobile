package id.laukpauk.data.model.delivery;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;

/**
 * The buyer's chosen delivery address + delivery schedule ("ADJD": Alamat Delivery dan Jadwal Delivery),
 * as saved on the device by {@link id.laukpauk.data.repository.DeliveryRepository}. Immutable.
 * {@link #NOT_SET} when the buyer has not chosen one yet.
 */
public final class DeliverySelection {

    public static final DeliverySelection NOT_SET = new DeliverySelection(false, null, null, null, null, null);

    /** True once both the address and the schedule are chosen (legacy {@code isDeliveryAddressAndScheduleSet}). */
    public final boolean isSet;
    @Nullable public final String latitude;
    @Nullable public final String longitude;
    @Json(name = "alamat")
    @Nullable public final String address;
    /** Server code of the chosen delivery schedule (sent as {@code deliveryScheduleCode}). */
    @Json(name = "jadwalDeliveryCode")
    @Nullable public final String deliveryScheduleCode;
    /** The schedule's day label (sent as {@code dayDelivery}; legacy {@code savedDeliveryScheduleMap.day}). */
    @Nullable public final String dayDelivery;

    public DeliverySelection(boolean isSet, @Nullable String latitude, @Nullable String longitude,
                             @Nullable String address, @Nullable String deliveryScheduleCode,
                             @Nullable String dayDelivery) {
        this.isSet = isSet;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.deliveryScheduleCode = deliveryScheduleCode;
        this.dayDelivery = dayDelivery;
    }
}
