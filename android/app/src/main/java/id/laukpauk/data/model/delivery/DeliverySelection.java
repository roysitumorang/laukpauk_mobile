package id.laukpauk.data.model.delivery;

import androidx.annotation.Nullable;

/**
 * The buyer's chosen delivery address + delivery schedule ("ADJD": Alamat Delivery dan Jadwal Delivery),
 * as saved on the device by {@link id.laukpauk.data.repository.DeliveryRepository}. Immutable.
 * {@link #NOT_SET} when the buyer has not chosen one yet.
 */
public final class DeliverySelection {

    public static final DeliverySelection NOT_SET = new DeliverySelection(false, null, null, null, null, null);

    /** True once both the address and the schedule are chosen (legacy {@code isAlamatDeliveryDanJadwalDeliverySet}). */
    public final boolean isSet;
    @Nullable public final String latitude;
    @Nullable public final String longitude;
    @Nullable public final String alamat;
    /** Server code of the chosen delivery schedule (sent as {@code jadwalDeliveryCode}). */
    @Nullable public final String jadwalDeliveryCode;
    /** The schedule's day label (sent as {@code dayDelivery}; legacy {@code savedJadwalDeliveryMap.day}). */
    @Nullable public final String dayDelivery;

    public DeliverySelection(boolean isSet, @Nullable String latitude, @Nullable String longitude,
                             @Nullable String alamat, @Nullable String jadwalDeliveryCode,
                             @Nullable String dayDelivery) {
        this.isSet = isSet;
        this.latitude = latitude;
        this.longitude = longitude;
        this.alamat = alamat;
        this.jadwalDeliveryCode = jadwalDeliveryCode;
        this.dayDelivery = dayDelivery;
    }
}
