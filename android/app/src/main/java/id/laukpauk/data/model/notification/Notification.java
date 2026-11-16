package id.laukpauk.data.model.notification;

import com.squareup.moshi.Json;

/** An "Info" notification (GET User/getNotifikasi, Notifikasi/getNotifikasi/{id}). */
public class Notification {
    @Json(name = "idNotifikasi")
    @Json(name = "idNotifikasi")
    public String notificationId;
    @Json(name = "judul")
    @Json(name = "judul")
    public String heading;
    @Json(name = "pesan")
    @Json(name = "pesan")
    public String message;
    /** File name in ServerPath.uploadImagePushNotification; empty when there is no picture. */
    @Json(name = "foto")
    @Json(name = "foto")
    public String photo;
    @Json(name = "isRead")
    public boolean isRead;
    @Json(name = "createdAt")
    public String createdAt;
}
