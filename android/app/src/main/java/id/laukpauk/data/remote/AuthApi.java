package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.auth.AuthResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Login, registration, OTP and password-recovery endpoints (JSON; ApiClient adds the auth and version headers). */
public interface AuthApi {

    @GET("Auth/loginAuthentication")
    Call<AuthResponses.Login> login(@Query("nomorHP") String nomorHP,
                                    @Query("password") String password,
                                    @Query("cartCode") String cartCode);

    @POST("Auth/register")
    Call<AuthResponses.Register> register(@Body Map<String, Object> body);

    @POST("Auth/konfirmasiOTP")
    Call<AuthResponses.KonfirmasiOTP> konfirmasiOTP(@Body Map<String, Object> body);

    @POST("Auth/verifikasiUser")
    Call<AuthResponses.VerifikasiUser> verifikasiUser(@Body Map<String, Object> body);

    @POST("Auth/resendOTP")
    Call<AuthResponses.ResendOTP> resendOTP(@Body Map<String, Object> body);

    @POST("Auth/lupaPassword")
    Call<AuthResponses.LupaPassword> lupaPassword(@Body Map<String, Object> body);

    @GET("User/getDetailUser")
    Call<AuthResponses.DetailUserResponse> getDetailUser(@Query("getUserEntities") String getUserEntities);
}
