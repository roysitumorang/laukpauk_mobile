package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.delivery.DeliveryResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** Delivery plan (schedule options), gifts, cart reset and the Google Maps web services (JSON; ApiClient adds the auth and version headers). */
public interface DeliveryApi {

    /**
     * The delivery plan of the current transaction window (buyer JWT required): its {@code delivery_times} are the
     * schedule options. 404 when no plan covers now.
     */
    @GET("buyer/delivery_plans/now")
    Call<DeliveryResponses.PlanNow> deliveryPlanNow();

    @GET("Gift/getGift")
    Call<DeliveryResponses.Gifts> getGifts();

    /** Legacy CheckoutModel.clearCart (the response is ignored). */
    @GET("Keranjang/bersihkanKeranjang")
    Call<Map<String, Object>> clearCart(@Query("cartCode") String cartCode);

    @GET("https://maps.googleapis.com/maps/api/geocode/json")
    Call<DeliveryResponses.Geocode> reverseGeocode(@Query("latlng") String latlng, @Query("key") String key);

    @GET("https://maps.googleapis.com/maps/api/place/textsearch/json")
    Call<DeliveryResponses.PlaceSearch> searchPlaces(@Query("query") String query, @Query("key") String key);
}
