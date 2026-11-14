package id.laukpauk.data.remote;

import id.laukpauk.data.model.market.PasarResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Market endpoints (versionCode is added by ApiClient). */
public interface PasarApi {

    /** Products of a market for the signed-in user. */
    @GET("Pasar/getProdukInThisPasar/{idPasar}")
    Call<PasarResponses.ProdukList> getProdukInThisPasar(@Path("idPasar") String idPasar,
                                                         @Query("authToken") String authToken,
                                                         @Query("jadwalDeliveryCode") String jadwalDeliveryCode,
                                                         @Query("startFrom") String startFrom,
                                                         @Query("useLimit") String useLimit,
                                                         @Query("cariProduk") String cariProduk);

    @GET("Pasar/getPasarAroundOfThisLatLong")
    Call<PasarResponses.PasarList> getPasarAround(@Query("latitude") String latitude,
                                                  @Query("longitude") String longitude,
                                                  @Query("exclude") String exclude,
                                                  @Query("jadwalDeliveryCode") String jadwalDeliveryCode);

    @GET("Pasar/getPasarOverOfThisLatLong")
    Call<PasarResponses.PasarList> getPasarOver(@Query("latitude") String latitude,
                                                @Query("longitude") String longitude,
                                                @Query("jadwalDeliveryCode") String jadwalDeliveryCode);
}
