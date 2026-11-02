package id.laukpauk.data.remote;

import id.laukpauk.data.model.account.AccountResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** The signed-in user: profile, wallet, vouchers, points (versionCode is added by ApiClient). */
public interface UserApi {

    /** {@code getUserEntities}: optional comma separated column list (legacy "select"). */
    @GET("User/getDetailUser")
    Call<AccountResponses.DetailUser> getDetailUser(@Query("authToken") String authToken,
                                                    @Query("getUserEntities") String select);

    @GET("User/getTotalDompet")
    Call<AccountResponses.TotalDompet> getTotalDompet(@Query("authToken") String authToken);

    @GET("User/getJumlahVoucher")
    Call<AccountResponses.JumlahVoucher> getJumlahVoucher(@Query("authToken") String authToken);

    /** Legacy getPoint("point=true"). */
    @GET("Point/getPoint")
    Call<AccountResponses.Point> getPoint(@Query("authToken") String authToken, @Query("point") String point);

    /** {@code jenisDompet}: "referral", "potongantransaksi" or null for all. */
    @GET("User/getRiwayatDompet")
    Call<AccountResponses.RiwayatDompetList> getRiwayatDompet(@Query("authToken") String authToken,
                                                              @Query("jenisDompet") String jenisDompet);

    @GET("User/getRiwayatPoint")
    Call<AccountResponses.RiwayatPointList> getRiwayatPoint(@Query("authToken") String authToken);

    @FormUrlEncoded
    @POST("User/updateProfil")
    Call<AccountResponses.UpdateProfil> updateProfil(@Query("authToken") String authToken,
                                                     @Field("namaLengkap") String namaLengkap,
                                                     @Field("alamat") String alamat,
                                                     @Field("username") String username);

    @FormUrlEncoded
    @POST("User/updateFotoProfil")
    Call<AccountResponses.UpdateFoto> updateFotoProfil(@Query("authToken") String authToken,
                                                       @Field("fotoProfil") String fotoProfilBase64);

    @FormUrlEncoded
    @POST("User/gantiPassword")
    Call<AccountResponses.GantiPassword> gantiPassword(@Query("authToken") String authToken,
                                                       @Field("passwordLama") String passwordLama,
                                                       @Field("passwordBaru") String passwordBaru,
                                                       @Field("konfirmasiPasswordBaru") String konfirmasi);
}
