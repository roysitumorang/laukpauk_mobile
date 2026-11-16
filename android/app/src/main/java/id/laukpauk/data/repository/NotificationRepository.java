package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.notification.Notification;
import id.laukpauk.data.model.notification.SpecialNotification;
import id.laukpauk.data.model.notification.NotificationResponses;
import id.laukpauk.data.remote.NotificationApi;

/**
 * Notifications: the "Info" broadcast list, the personal list, details, read status, and the unread
 * counter shown as the bell badge in the header.
 */
public class NotificationRepository extends BaseRepository {

    public static final String INFO_TYPE = "update";
    public static final String SPECIAL_TYPE = "khususuntukkamu";

    private final NotificationApi api;
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);

    public NotificationRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(NotificationApi.class);
    }

    /** "Info" notifications (empty list when the server returns nothing). */
    public LiveData<Resource<List<Notification>>> getNotificationInfo() {
        return load(api.getNotification(), body -> nonNull(body == null ? null : body.notification));
    }

    /** Personal notifications of the signed-in user. */
    public LiveData<Resource<List<SpecialNotification>>> getSpecialNotification() {
        return load(api.getSpecialNotification(), body -> nonNull(body == null ? null : body.notification));
    }

    /** One "Info" notification; SUCCESS with null data when the server says it doesn't exist. */
    public LiveData<Resource<Notification>> getNotificationDetail(String notificationId) {
        return load(api.getNotificationDetail(notificationId), body -> body == null ? null : body.notification);
    }

    /** Marks a personal notification as read; the result is the server's {@code statusSetAs}. */
    public void setAsRead(String notificationItemId, ResultCallback<Boolean> callback) {
        enqueue(api.setAs(body("idNotifikasiItem", notificationItemId, "setAs", "read", "jenisNotifikasi", SPECIAL_TYPE)),
                body -> body != null && body.statusSetAs, callback);
    }

    /** Unread Info + personal notifications (0 when signed out). Observed by the header's bell badge. */
    public LiveData<Integer> unreadCount() {
        return unreadCount;
    }

    /**
     * Reloads {@link #unreadCount()} from the server: the sum of the unread "Info" and personal counts,
     * like the Dart HeaderModel.getNotificationCount(). Signed out: 0 without a request.
     */
    public void refreshUnreadCount() {
        if (authToken().isEmpty()) {
            unreadCount.setValue(0);
            return;
        }
        final int[] parts = new int[2];
        final int[] pending = {2};
        ResultCallback<Integer> done = r -> {
            if (--pending[0] == 0) {
                unreadCount.setValue(parts[0] + parts[1]);
            }
        };
        enqueue(api.getNotificationCount("unread"),
                body -> parts[0] = body == null ? 0 : body.notificationCount, done);
        enqueue(api.getSpecialNotificationCount("unread"),
                body -> parts[1] = body == null ? 0 : body.notificationCount, done);
    }

    private static <T> List<T> nonNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
