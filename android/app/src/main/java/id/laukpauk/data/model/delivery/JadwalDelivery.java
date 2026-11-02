package id.laukpauk.data.model.delivery;

import java.util.Map;

import androidx.annotation.Nullable;


import id.laukpauk.util.Json;

/**
 * One available delivery schedule (JadwalDelivery/getAvailableJadwalDelivery). The server object is kept
 * whole in {@link #raw} (dynamic payload): the legacy app stored it verbatim as {@code savedJadwalDeliveryMap}
 * (plus {@code setOn}) and the expiry check reads {@code id}, {@code day}, {@code maxTime}, {@code currentDate}.
 */
public final class JadwalDelivery {
    public final Map<String, Object> raw;
    @Nullable public final String kode;
    public final String day;
    public final String sessi;
    public final String rentangWaktu;
    public final boolean isFull;
    public final boolean isOpened;

    public JadwalDelivery(Map<String, Object> raw) {
        this.raw = raw;
        this.kode = Json.strOrNull(raw, "kode");
        this.day = Json.str(raw, "day");
        this.sessi = Json.str(raw, "sessi");
        this.rentangWaktu = Json.str(raw, "rentangWaktu");
        this.isFull = Json.bool(raw, "isFull");
        this.isOpened = Json.bool(raw, "isOpened");
    }

    /** "Besok Pagi" style identity used to mark the selected tile (legacy readAbleJadwalPengantaran). */
    public String readable() {
        return day + " " + sessi;
    }

    /** Legacy: tappable only when neither full nor closed. */
    public boolean isSelectable() {
        return !isFull && isOpened;
    }
}
