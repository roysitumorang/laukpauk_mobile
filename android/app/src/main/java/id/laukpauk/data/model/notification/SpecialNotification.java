package id.laukpauk.data.model.notification;

import com.squareup.moshi.Json;

/** A personal ("Khusus untuk kamu") notification (GET User/getNotifikasiKhusus). */
public class SpecialNotification {
    @Json(name = "id")
    public String id;
    @Json(name = "judul")
    public String heading;
    @Json(name = "isi")
    public String body;
    /** Route opened when tapped (see ui/startup/MainRouter), e.g. "/orderan". */
    @Json(name = "routeName")
    public String routeName;
    /** Route arguments as JSON text, e.g. {"transactionId":"12"}. */
    @Json(name = "routeParams")
    public String routeParams;
    @Json(name = "isRead")
    public boolean isRead;
    /** Icon name in assets/img/notifikasi/ (orderan, dompet). */
    @Json(name = "jenisNotifikasi")
    public String notificationType;
    @Json(name = "waktu")
    public String time;
}
