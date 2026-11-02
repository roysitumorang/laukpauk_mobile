package id.laukpauk.data.remote;

import id.laukpauk.data.model.orders.TransaksiResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Buyer transaction endpoints (versionCode is added by ApiClient). */
public interface TransaksiApi {

    @GET("User/getRiwayatTransaksi")
    Call<TransaksiResponses.RiwayatList> getRiwayatTransaksi(
            @Query("authToken") String authToken,
            @Query("statusTransaksi") String statusTransaksi,
            @Query("statusTransaksiHariIni") String statusTransaksiHariIni,
            @Query("rentangAwal") String rentangAwal,
            @Query("rentangAkhir") String rentangAkhir);

    @GET("Transaksi/getTransaksi/{id}")
    Call<TransaksiResponses.Detail> getTransaksi(@Path("id") String id,
                                                 @Query("authToken") String authToken,
                                                 @Query("entitasTransaksi") String entitasTransaksi);

    @GET("Transaksi/getItemTransaksiFromTransaksi/{id}")
    Call<TransaksiResponses.Items> getItemTransaksi(@Path("id") String id,
                                                    @Query("authToken") String authToken,
                                                    @Query("entitasItemTransaksi") String entitasItemTransaksi,
                                                    @Query("idPenjual") String idPenjual);

    @GET("Transaksi/getJumlahItemDiantar/{id}")
    Call<TransaksiResponses.Diantar> getJumlahItemDiantar(@Path("id") String id,
                                                          @Query("authToken") String authToken);

    /** POST without a form body. */
    @POST("Transaksi/pesananDiterima/{id}")
    Call<TransaksiResponses.PesananDiterima> pesananDiterima(@Path("id") String id,
                                                             @Query("authToken") String authToken);

    @FormUrlEncoded
    @POST("Transaksi/belanjaUlang/{id}")
    Call<TransaksiResponses.BelanjaUlang> belanjaUlang(@Path("id") String id,
                                                       @Field("dayDelivery") String dayDelivery,
                                                       @Field("jadwalDeliveryCode") String jadwalDeliveryCode,
                                                       @Field("cartCode") String cartCode);

    @GET("gift/gift-transaksi")
    Call<TransaksiResponses.GiftList> getGiftTransaksi(@Query("cartCode") String cartCode);

    @GET("Gift/getGift/{idGift}")
    Call<TransaksiResponses.GiftDetail> getGift(@Path("idGift") String idGift);
}
