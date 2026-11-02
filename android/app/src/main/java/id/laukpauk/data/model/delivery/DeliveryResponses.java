package id.laukpauk.data.model.delivery;

import java.util.Map;

import com.squareup.moshi.Json;

import java.util.List;

/** Response wrappers of the delivery endpoints and of the Google geocoding / places web services. */
public final class DeliveryResponses {
    private DeliveryResponses() {}

    /** {@code {"availableJadwalDelivery": [...]}}; the items are dynamic objects, see {@link JadwalDelivery}. */
    public static class Jadwal {
        public List<Map<String, Object>> availableJadwalDelivery;
    }

    /** {@code {"gift": [...]}}. */
    public static class Gifts {
        public List<Gift> gift;
    }

    /** Google Geocoding API. */
    public static class Geocode {
        public List<GeocodeResult> results;
    }

    public static class GeocodeResult {
        @Json(name = "formatted_address")
        public String formattedAddress;
    }

    /** Google Places Text Search API. */
    public static class PlaceSearch {
        public List<PlaceResult> results;
    }

    public static class PlaceResult {
        public String name;
        @Json(name = "formatted_address")
        public String formattedAddress;
        public Geometry geometry;
    }

    public static class Geometry {
        public LatLng location;
    }

    public static class LatLng {
        public double lat;
        public double lng;
    }
}
