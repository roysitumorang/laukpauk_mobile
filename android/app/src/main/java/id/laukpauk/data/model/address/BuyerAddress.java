package id.laukpauk.data.model.address;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

/** One of the buyer's delivery addresses ({@code buyer/delivery_addresses}). */
public class BuyerAddress {
    @Nullable public String id;
    /** Decimal degrees of the delivery point. */
    @Json(name = "destination_longitude")
    @Nullable public String destinationLongitude;
    @Json(name = "destination_latitude")
    @Nullable public String destinationLatitude;
    /** Road distance from the store, rounded up to whole kilometres (set by the backend). */
    @Json(name = "distance_km")
    public long distanceKm;
    @Json(name = "street_address")
    @Nullable public String streetAddress;
    /** A note for the courier, e.g. "Pagar hijau". */
    @Json(name = "street_address_note")
    @Nullable public String streetAddressNote;
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
        return destinationLatitude != null && !destinationLatitude.trim().isEmpty()
                && destinationLongitude != null && !destinationLongitude.trim().isEmpty();
    }
}
