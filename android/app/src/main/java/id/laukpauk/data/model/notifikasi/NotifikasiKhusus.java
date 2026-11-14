package id.laukpauk.data.model.notifikasi;

import com.squareup.moshi.Json;

/** A personal ("Khusus untuk kamu") notification (GET User/getNotifikasiKhusus). */
public class NotifikasiKhusus {
    @Json(name = "id")
    public String id;
    @Json(name = "judul")
    public String judul;
    @Json(name = "isi")
    public String isi;
    /** Route opened when tapped (see ui/startup/MainRouter), e.g. "/orderan". */
    @Json(name = "routeName")
    public String routeName;
    /** Route arguments as JSON text, e.g. {"idTransaksi":"12"}. */
    @Json(name = "routeParams")
    public String routeParams;
    @Json(name = "isRead")
    public boolean isRead;
    /** Icon name in assets/img/notifikasi/ (orderan, dompet). */
    @Json(name = "jenisNotifikasi")
    public String jenisNotifikasi;
    @Json(name = "waktu")
    public String waktu;
}
