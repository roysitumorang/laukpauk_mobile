package id.laukpauk.data.remote;

import id.laukpauk.data.model.notifikasi.NotifikasiResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Notification endpoints (versionCode is added by ApiClient). */
public interface NotifikasiApi {

    @GET("User/getNotifikasi")
    Call<NotifikasiResponses.InfoList> getNotifikasi(@Query("authToken") String authToken);

    @GET("User/getNotifikasiKhusus")
    Call<NotifikasiResponses.KhususList> getNotifikasiKhusus(@Query("authToken") String authToken);

    @GET("User/getNotifikasiKhusus/{idUser}")
    Call<NotifikasiResponses.KhususList> getNotifikasiKhusus(@Path("idUser") String idUser,
                                                            @Query("authToken") String authToken);

    @GET("User/getJumlahNotifikasi")
    Call<NotifikasiResponses.Count> getJumlahNotifikasi(@Query("status") String status,
                                                        @Query("authToken") String authToken);

    @GET("User/getJumlahNotifikasiKhusus")
    Call<NotifikasiResponses.Count> getJumlahNotifikasiKhusus(@Query("status") String status,
                                                              @Query("authToken") String authToken);

    @GET("Notifikasi/getNotifikasi/{idNotifikasi}")
    Call<NotifikasiResponses.Detail> getDetailNotifikasi(@Path("idNotifikasi") String idNotifikasi,
                                                         @Query("authToken") String authToken);

    @FormUrlEncoded
    @POST("Notifikasi/setAs")
    Call<NotifikasiResponses.SetAs> setAs(@Query("authToken") String authToken,
                                          @Field("idNotifikasiItem") String idNotifikasiItem,
                                          @Field("setAs") String setAs,
                                          @Field("jenisNotifikasi") String jenisNotifikasi);
}
