package id.laukpauk.data.remote;

import java.util.Map;


import id.laukpauk.data.model.delivery.DeliveryResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** Delivery schedule, gifts, cart reset and the Google Maps web services (versionCode is added by ApiClient). */
public interface DeliveryApi {

    @GET("JadwalDelivery/getAvailableJadwalDelivery")
    Call<DeliveryResponses.Jadwal> getAvailableJadwalDelivery();

    @GET("Gift/getGift")
    Call<DeliveryResponses.Gifts> getGifts();

    /** Legacy CheckoutModel.bersihkanKeranjang (the response is ignored). */
    @GET("Keranjang/bersihkanKeranjang")
    Call<Map<String, Object>> bersihkanKeranjang(@Query("cartCode") String cartCode);

    @GET("https://maps.googleapis.com/maps/api/geocode/json")
    Call<DeliveryResponses.Geocode> reverseGeocode(@Query("latlng") String latlng, @Query("key") String key);

    @GET("https://maps.googleapis.com/maps/api/place/textsearch/json")
    Call<DeliveryResponses.PlaceSearch> searchPlaces(@Query("query") String query, @Query("key") String key);
}
