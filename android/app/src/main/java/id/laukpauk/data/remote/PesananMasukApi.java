package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.seller.orders.PesananMasukResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Seller incoming-order endpoints (versionCode is added by ApiClient). */
public interface PesananMasukApi {

    @GET("Penjual/getPesananMasuk")
    Call<PesananMasukResponses.Daftar> getPesananMasuk(@Query("authToken") String authToken,
                                                      @Query("statusTransaksi") String statusTransaksi);

    @GET("Transaksi/getTransaksi/{id}")
    Call<PesananMasukResponses.Detail> getTransaksi(@Path("id") String id, @Query("authToken") String authToken);

    @GET("Penjual/getPesananMasukItem/{id}")
    Call<PesananMasukResponses.Items> getPesananMasukItem(@Path("id") String id,
                                                         @Query("authToken") String authToken,
                                                         @Query("pesananEntity") String pesananEntity);

    /** A POST without a body in the legacy app; the map is empty (versionCode is added by the interceptor). */
    @FormUrlEncoded
    @POST("Transaksi/prosesPesanan/{id}")
    Call<PesananMasukResponses.Status> prosesPesanan(@Path("id") String id, @Query("authToken") String authToken,
                                                    @FieldMap Map<String, String> empty);

    @FormUrlEncoded
    @POST("Transaksi/pembatalanKlaimTidakAda")
    Call<PesananMasukResponses.Status> pembatalanKlaimTidakAda(@Query("authToken") String authToken,
                                                              @Field("idItem") String idItem);

    @FormUrlEncoded
    @POST("Transaksi/klaimProdukTidakTersedia")
    Call<PesananMasukResponses.Status> klaimProdukTidakTersedia(@Query("authToken") String authToken,
                                                               @Field("idItem") String idItem,
                                                               @Field("keterangan") String keterangan);
}
