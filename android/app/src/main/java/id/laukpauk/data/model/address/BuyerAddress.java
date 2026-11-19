package id.laukpauk.data.model.address;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

/** One of the buyer's delivery addresses ({@code GET/POST buyer/addresses}). */
public class BuyerAddress {
    @Nullable public String id;
    @Nullable public String longitude;
    @Nullable public String latitude;
    @Json(name = "street_address")
    @Nullable public String streetAddress;
    @Json(name = "delivery_note")
    @Nullable public String deliveryNote;
    /** e.g. "Rumah", "Kantor". */
    @Nullable public String label;
    @Json(name = "recipient_name")
    @Nullable public String recipientName;
    /** E.164, e.g. +6281234567890. */
    @Json(name = "recipient_mobile_phone")
    @Nullable public String recipientMobilePhone;
    /** The buyer's main address (listed first). */
    public boolean main;

    public boolean hasLocation() {
        return latitude != null && !latitude.trim().isEmpty() && longitude != null && !longitude.trim().isEmpty();
    }
}
