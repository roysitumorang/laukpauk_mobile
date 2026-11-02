package id.laukpauk.data.model.notifikasi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Response envelopes of the notification endpoints. */
public final class NotifikasiResponses {
    private NotifikasiResponses() {}

    public static class InfoList {
        @SerializedName("notifikasi")
        public List<Notifikasi> notifikasi;
    }

    public static class KhususList {
        @SerializedName("notifikasi")
        public List<NotifikasiKhusus> notifikasi;
    }

    public static class Detail {
        /** null when the server answers false ("not registered"). */
        @SerializedName("notifikasi")
        public Notifikasi notifikasi;
    }

    public static class Count {
        @SerializedName("jumlahNotifikasi")
        public int jumlahNotifikasi;
    }

    public static class SetAs {
        @SerializedName("statusSetAs")
        public boolean statusSetAs;
    }
}
