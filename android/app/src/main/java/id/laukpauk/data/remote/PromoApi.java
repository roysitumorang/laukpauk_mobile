package id.laukpauk.data.remote;

import id.laukpauk.data.model.promo.PromoResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Promo endpoints (versionCode is added by ApiClient). */
public interface PromoApi {

    @GET("Promo/getPromoAroundOfThisLatLong")
    Call<PromoResponses.ProdukPromoList> getPromoAroundOfThisLatLong(
            @Query("authToken") String authToken,
            @Query("latitude") String latitude,
            @Query("longitude") String longitude,
            @Query("jadwalDeliveryCode") String jadwalDeliveryCode);

    @GET("Promo/getPromoItem/{idPromoItem}")
    Call<PromoResponses.Detail> getPromoItem(@Path("idPromoItem") String idPromoItem,
                                             @Query("select") String select);
}
