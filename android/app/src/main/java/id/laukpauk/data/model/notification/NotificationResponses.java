package id.laukpauk.data.model.notification;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the notification endpoints. */
public final class NotificationResponses {
    private NotificationResponses() {}

    public static class InfoList {
        @Json(name = "notifikasi")
        @Json(name = "notifikasi")
        public List<Notification> notification;
    }

    public static class SpecialList {
        @Json(name = "notifikasi")
        @Json(name = "notifikasi")
        public List<SpecialNotification> notification;
    }

    public static class Detail {
        /** null when the server answers false ("not registered"). */
        @Json(name = "notifikasi")
        @Json(name = "notifikasi")
        public Notification notification;
    }

    public static class Count {
        @Json(name = "jumlahNotifikasi")
        @Json(name = "jumlahNotifikasi")
        public int notificationCount;
    }

    public static class SetAs {
        @Json(name = "statusSetAs")
        public boolean statusSetAs;
    }
}
