package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.account.AccountResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** The signed-in user: profile, wallet, vouchers, points (JSON; ApiClient adds the auth and version headers). */
public interface UserApi {

    /** {@code getUserEntities}: optional comma separated column list (legacy "select"). */
    @GET("User/getDetailUser")
    Call<AccountResponses.DetailUser> getDetailUser(@Query("getUserEntities") String select);

    @GET("User/getTotalDompet")
    Call<AccountResponses.TotalDompet> getTotalDompet();

    @GET("User/getJumlahVoucher")
    Call<AccountResponses.JumlahVoucher> getJumlahVoucher();

    /** Legacy getPoint("point=true"). */
    @GET("Point/getPoint")
    Call<AccountResponses.Point> getPoint(@Query("point") String point);

    /** {@code jenisDompet}: "referral", "potongantransaksi" or null for all. */
    @GET("User/getRiwayatDompet")
    Call<AccountResponses.RiwayatDompetList> getRiwayatDompet(@Query("jenisDompet") String jenisDompet);

    @GET("User/getRiwayatPoint")
    Call<AccountResponses.RiwayatPointList> getRiwayatPoint();

    @POST("User/updateProfil")
    Call<AccountResponses.UpdateProfil> updateProfil(@Body Map<String, Object> body);

    @POST("User/updateFotoProfil")
    Call<AccountResponses.UpdateFoto> updateFotoProfil(@Body Map<String, Object> body);

    @POST("User/gantiPassword")
    Call<AccountResponses.GantiPassword> gantiPassword(@Body Map<String, Object> body);
}
