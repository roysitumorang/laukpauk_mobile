package id.laukpauk.data.remote;


import id.laukpauk.data.model.account.AccountResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Auth-side account actions, and referral (versionCode is added by ApiClient). */
public interface AccountApi {

    @GET("Auth/removeToken")
    Call<AccountResponses.RemoveToken> removeToken(@Query("authToken") String authToken,
                                                   @Query("cartCode") String cartCode);

    @FormUrlEncoded
    @POST("Auth/gantiNomor")
    Call<AccountResponses.GantiNomor> gantiNomor(@Query("authToken") String authToken,
                                                 @Field("nomorBaru") String nomorBaru);

    @FormUrlEncoded
    @POST("Auth/verifikasiNomor")
    Call<AccountResponses.VerifikasiNomor> verifikasiNomor(@Query("authToken") String authToken,
                                                           @Field("idPenggantianNomor") String idPenggantianNomor,
                                                           @Field("kodeVerifikasi") String kodeVerifikasi);

    @GET("Auth/jumlahGantiNomorHPForToday")
    Call<AccountResponses.JumlahGantiNomor> jumlahGantiNomorHPForToday(@Query("authToken") String authToken);

    @GET("Pengaturan/getReferralConfiguration")
    Call<AccountResponses.ReferralConfiguration> getReferralConfiguration(@Query("authToken") String authToken);
}
