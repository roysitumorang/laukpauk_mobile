package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.notification.NotificationResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Notification endpoints (JSON; ApiClient adds the auth and version headers). */
public interface NotificationApi {

    @GET("User/getNotifikasi")
    Call<NotificationResponses.InfoList> getNotification();

    @GET("User/getNotifikasiKhusus")
    Call<NotificationResponses.SpecialList> getSpecialNotification();

    @GET("User/getNotifikasiKhusus/{idUser}")
    Call<NotificationResponses.SpecialList> getSpecialNotification(@Path("idUser") String idUser);

    @GET("User/getJumlahNotifikasi")
    Call<NotificationResponses.Count> getNotificationCount(@Query("status") String status);

    @GET("User/getJumlahNotifikasiKhusus")
    Call<NotificationResponses.Count> getSpecialNotificationCount(@Query("status") String status);

    @GET("Notifikasi/getNotifikasi/{idNotifikasi}")
    Call<NotificationResponses.Detail> getNotificationDetail(@Path("idNotifikasi") String notificationId);

    @POST("Notifikasi/setAs")
    Call<NotificationResponses.SetAs> setAs(@Body Map<String, Object> body);
}
