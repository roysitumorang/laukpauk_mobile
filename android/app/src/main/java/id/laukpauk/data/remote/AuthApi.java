package id.laukpauk.data.remote;

import id.laukpauk.data.model.auth.AuthResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Login, registration, OTP and password-recovery endpoints (versionCode is added by ApiClient). */
public interface AuthApi {

    @GET("Auth/loginAuthentication")
    Call<AuthResponses.Login> login(@Query("nomorHP") String nomorHP, @Query("password") String password,
                                    @Query("cartCode") String cartCode);

    @FormUrlEncoded
    @POST("Auth/register")
    Call<AuthResponses.Register> register(@Field("namaLengkap") String namaLengkap,
                                          @Field("nomorHP") String nomorHP,
                                          @Field("password") String password,
                                          @Field("konfirmasiPassword") String konfirmasiPassword,
                                          @Field("kodeReferal") String kodeReferal);

    @FormUrlEncoded
    @POST("Auth/konfirmasiOTP")
    Call<AuthResponses.KonfirmasiOTP> konfirmasiOTP(@Field("idUser") String idUser, @Field("otp") String otp);

    @FormUrlEncoded
    @POST("Auth/verifikasiUser")
    Call<AuthResponses.VerifikasiUser> verifikasiUser(@Field("idUser") String idUser,
                                                      @Field("verificationCode") String verificationCode,
                                                      @Field("isSeller") String isSeller,
                                                      @Field("idPasar") String idPasar);

    @FormUrlEncoded
    @POST("Auth/resendOTP")
    Call<AuthResponses.ResendOTP> resendOTP(@Query("authToken") String authToken,
                                            @Field("nomorTelepon") String nomorTelepon);

    @FormUrlEncoded
    @POST("Auth/lupaPassword")
    Call<AuthResponses.LupaPassword> lupaPassword(@Field("nomorHP") String nomorHP);

    @GET("User/getDetailUser")
    Call<AuthResponses.DetailUserResponse> getDetailUser(@Query("authToken") String authToken,
                                                         @Query("getUserEntities") String getUserEntities);

    @GET("Pasar/getPasar")
    Call<AuthResponses.PasarList> getPasar(@Query("jenisPasar") String jenisPasar);
}
