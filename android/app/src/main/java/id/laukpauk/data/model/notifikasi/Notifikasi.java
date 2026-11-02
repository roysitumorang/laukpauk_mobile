package id.laukpauk.data.model.notifikasi;

import com.squareup.moshi.Json;

/** An "Info" notification (GET User/getNotifikasi, Notifikasi/getNotifikasi/{id}). */
public class Notifikasi {
    @Json(name = "idNotifikasi")
    public String idNotifikasi;
    @Json(name = "judul")
    public String judul;
    @Json(name = "pesan")
    public String pesan;
    /** File name in ServerPath.uploadGambarPushNotifikasi; empty when there is no picture. */
    @Json(name = "foto")
    public String foto;
    @Json(name = "isRead")
    public boolean isRead;
    @Json(name = "createdAt")
    public String createdAt;
}
