package id.laukpauk.data.model.notifikasi;

import com.google.gson.annotations.SerializedName;

/** An "Info" notification (GET User/getNotifikasi, Notifikasi/getNotifikasi/{id}). */
public class Notifikasi {
    @SerializedName("idNotifikasi")
    public String idNotifikasi;
    @SerializedName("judul")
    public String judul;
    @SerializedName("pesan")
    public String pesan;
    /** File name in ServerPath.uploadGambarPushNotifikasi; empty when there is no picture. */
    @SerializedName("foto")
    public String foto;
    @SerializedName("isRead")
    public boolean isRead;
    @SerializedName("createdAt")
    public String createdAt;
}
