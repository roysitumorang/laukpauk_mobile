package id.laukpauk.data.model.delivery;

import java.util.Map;

import com.squareup.moshi.Json;

import java.util.List;

/** Response wrappers of the delivery endpoints and of the Google geocoding / places web services. */
public final class DeliveryResponses {
    private DeliveryResponses() {}

    /** {@code GET buyer/delivery_plans/now}: the backend envelope; the plan is in {@code data}. */
    public static class PlanNow {
        @androidx.annotation.Nullable public DeliveryPlan data;
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
