package id.laukpauk.data.remote;

import id.laukpauk.data.model.seller.shop.TokoSayaResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/** Seller shop dashboard endpoints (versionCode is added by ApiClient). */
public interface TokoSayaApi {

    @GET("User/getDetailUser")
    Call<TokoSayaResponses.DetailUser> getDetailUserId(@Query("getUserEntities") String entities,
                                                       @Query("authToken") String authToken);

    @GET("Penjual/getDetailPenjual/{idPenjual}")
    Call<TokoSayaResponses.Detail> getDetailPenjual(@Path("idPenjual") String idPenjual,
                                                    @Query("authToken") String authToken);

    /** Legacy RequestPencairanDana: only some columns, no authToken. */
    @GET("Penjual/getDetailPenjual/{idPenjual}")
    Call<TokoSayaResponses.Detail> getDetailPenjualEntities(@Path("idPenjual") String idPenjual,
                                                            @Query("getPenjualEntities") String entities);

    @GET("Penjual/getTotalPenjualan")
    Call<TokoSayaResponses.TotalPenjualan> getTotalPenjualan(@Query("authToken") String authToken);

    @GET("Penjual/getTotalSaldo")
    Call<TokoSayaResponses.TotalSaldo> getTotalSaldo(@Query("authToken") String authToken);

    @GET("TagihanPenjual/getTagihan")
    Call<TokoSayaResponses.Tagihan> getTagihan(@Query("authToken") String authToken);

    @GET("Penjual/getJumlahProdukPenjual")
    Call<TokoSayaResponses.JumlahProduk> getJumlahProdukPenjual(@Query("authToken") String authToken);

    @GET("Penjual/getJadwalDeliveryPenjual")
    Call<TokoSayaResponses.JadwalList> getJadwalDeliveryPenjual(@Query("authToken") String authToken);

    @FormUrlEncoded
    @POST("Penjual/toggleJadwalDelivery")
    Call<TokoSayaResponses.Toggle> toggleJadwalDelivery(@Query("authToken") String authToken,
                                                        @Field("jadwalDeliveryCode") String jadwalDeliveryCode);

    @GET("Pasar/getPasar")
    Call<TokoSayaResponses.PasarList> getPasar(@Query("jenisPasar") String jenisPasar);

    /** Google reverse geocoding (full URL). */
    @GET
    Call<TokoSayaResponses.Geocode> reverseGeocode(@Url String url);

    /** Null fields are not sent: idPasarBaru for a listed market, latPasarBaru/longPasarBaru for an own location. */
    @FormUrlEncoded
    @POST("Penjual/ubahLokasiBerjualan/{idPenjual}")
    Call<TokoSayaResponses.UbahLokasi> ubahLokasiBerjualan(@Path("idPenjual") String idPenjual,
                                                           @Query("authToken") String authToken,
                                                           @Field("jenisPasarBaru") String jenisPasarBaru,
                                                           @Field("idPasarBaru") String idPasarBaru,
                                                           @Field("latPasarBaru") String latPasarBaru,
                                                           @Field("longPasarBaru") String longPasarBaru);

    @FormUrlEncoded
    @POST("Penjual/ubahNamaToko")
    Call<TokoSayaResponses.UbahNama> ubahNamaToko(@Query("authToken") String authToken,
                                                  @Field("namaToko") String namaToko);

    @FormUrlEncoded
    @POST("Penjual/ajukanRPD")
    Call<TokoSayaResponses.AjukanRpd> ajukanRpd(@Query("authToken") String authToken,
                                                @Field("nomorRekening") String nomorRekening,
                                                @Field("namaPenerima") String namaPenerima,
                                                @Field("nominal") String nominal,
                                                @Field("keterangan") String keterangan);
}
