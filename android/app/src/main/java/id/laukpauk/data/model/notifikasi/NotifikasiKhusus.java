package id.laukpauk.data.model.notifikasi;

import com.google.gson.annotations.SerializedName;

/** A personal ("Khusus untuk kamu") notification (GET User/getNotifikasiKhusus). */
public class NotifikasiKhusus {
    @SerializedName("id")
    public String id;
    @SerializedName("judul")
    public String judul;
    @SerializedName("isi")
    public String isi;
    /** Route opened when tapped (see ui/startup/MainRouter), e.g. "/orderan". */
    @SerializedName("routeName")
    public String routeName;
    /** Route arguments as JSON text, e.g. {"idTransaksi":"12"}. */
    @SerializedName("routeParams")
    public String routeParams;
    @SerializedName("isRead")
    public boolean isRead;
    /** Icon name in assets/img/notifikasi/ (orderan, dompet, penjualan, rmp, rpd). */
    @SerializedName("jenisNotifikasi")
    public String jenisNotifikasi;
    @SerializedName("waktu")
    public String waktu;
}
