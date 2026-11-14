package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.notifikasi.NotifikasiResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Notification endpoints (JSON; ApiClient adds the auth and version headers). */
public interface NotifikasiApi {

    @GET("User/getNotifikasi")
    Call<NotifikasiResponses.InfoList> getNotifikasi();

    @GET("User/getNotifikasiKhusus")
    Call<NotifikasiResponses.KhususList> getNotifikasiKhusus();

    @GET("User/getNotifikasiKhusus/{idUser}")
    Call<NotifikasiResponses.KhususList> getNotifikasiKhusus(@Path("idUser") String idUser);

    @GET("User/getJumlahNotifikasi")
    Call<NotifikasiResponses.Count> getJumlahNotifikasi(@Query("status") String status);

    @GET("User/getJumlahNotifikasiKhusus")
    Call<NotifikasiResponses.Count> getJumlahNotifikasiKhusus(@Query("status") String status);

    @GET("Notifikasi/getNotifikasi/{idNotifikasi}")
    Call<NotifikasiResponses.Detail> getDetailNotifikasi(@Path("idNotifikasi") String idNotifikasi);

    @POST("Notifikasi/setAs")
    Call<NotifikasiResponses.SetAs> setAs(@Body Map<String, Object> body);
}
