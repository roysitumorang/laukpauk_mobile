package id.laukpauk.data.remote;

import java.util.Map;


import id.laukpauk.data.model.account.AccountResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Auth-side account actions, and referral (JSON; ApiClient adds the auth and version headers). */
public interface AccountApi {

    @GET("Auth/removeToken")
    Call<AccountResponses.RemoveToken> removeToken(@Query("cartCode") String cartCode);

    @POST("Auth/gantiNomor")
    Call<AccountResponses.GantiNomor> gantiNomor(@Body Map<String, Object> body);

    @POST("Auth/verifikasiNomor")
    Call<AccountResponses.VerifikasiNomor> verifikasiNomor(@Body Map<String, Object> body);

    @GET("Auth/jumlahGantiNomorHPForToday")
    Call<AccountResponses.JumlahGantiNomor> jumlahGantiNomorHPForToday();

    @GET("Pengaturan/getReferralConfiguration")
    Call<AccountResponses.ReferralConfiguration> getReferralConfiguration();
}
