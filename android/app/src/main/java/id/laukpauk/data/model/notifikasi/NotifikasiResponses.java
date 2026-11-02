package id.laukpauk.data.model.notifikasi;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the notification endpoints. */
public final class NotifikasiResponses {
    private NotifikasiResponses() {}

    public static class InfoList {
        @Json(name = "notifikasi")
        public List<Notifikasi> notifikasi;
    }

    public static class KhususList {
        @Json(name = "notifikasi")
        public List<NotifikasiKhusus> notifikasi;
    }

    public static class Detail {
        /** null when the server answers false ("not registered"). */
        @Json(name = "notifikasi")
        public Notifikasi notifikasi;
    }

    public static class Count {
        @Json(name = "jumlahNotifikasi")
        public int jumlahNotifikasi;
    }

    public static class SetAs {
        @Json(name = "statusSetAs")
        public boolean statusSetAs;
    }
}
