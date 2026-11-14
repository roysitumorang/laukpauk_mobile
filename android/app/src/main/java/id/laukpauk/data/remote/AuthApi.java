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

    /**
     * Sign-in. Body: {@code login} (string) and {@code password} (string, base64 of the UTF-8 password). Answers {@code id_token} (the JWT),
     * {@code expired_at} (RFC 3339 date-time) and {@code user} (the signed-in user), either at the top level
     * or inside the backend's {@code data} envelope. Wrong credentials answer a 4xx status.
     */
    @POST("users/sign_in")
    Call<Map<String, Object>> signIn(@Body Map<String, Object> body);

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
