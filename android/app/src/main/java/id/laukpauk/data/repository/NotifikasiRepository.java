package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.notifikasi.Notifikasi;
import id.laukpauk.data.model.notifikasi.NotifikasiKhusus;
import id.laukpauk.data.model.notifikasi.NotifikasiResponses;
import id.laukpauk.data.remote.NotifikasiApi;

/**
 * Notifications: the "Info" broadcast list, the personal list, details, read status, and the unread
 * counter shown as the bell badge in the header.
 */
public class NotifikasiRepository extends BaseRepository {

    public static final String JENIS_INFO = "update";
    public static final String JENIS_KHUSUS = "khususuntukkamu";

    private final NotifikasiApi api;
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);

    public NotifikasiRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(NotifikasiApi.class);
    }

    /** "Info" notifications (empty list when the server returns nothing). */
    public LiveData<Resource<List<Notifikasi>>> getNotifikasiInfo() {
        return load(api.getNotifikasi(), body -> nonNull(body == null ? null : body.notifikasi));
    }

    /** Personal notifications of the signed-in user. */
    public LiveData<Resource<List<NotifikasiKhusus>>> getNotifikasiKhusus() {
        return load(api.getNotifikasiKhusus(), body -> nonNull(body == null ? null : body.notifikasi));
    }

    /** One "Info" notification; SUCCESS with null data when the server says it doesn't exist. */
    public LiveData<Resource<Notifikasi>> getDetailNotifikasi(String idNotifikasi) {
        return load(api.getDetailNotifikasi(idNotifikasi), body -> body == null ? null : body.notifikasi);
    }

    /** Marks a personal notification as read; the result is the server's {@code statusSetAs}. */
    public void setAsRead(String idNotifikasiItem, ResultCallback<Boolean> callback) {
        enqueue(api.setAs(body("idNotifikasiItem", idNotifikasiItem, "setAs", "read", "jenisNotifikasi", JENIS_KHUSUS)),
                body -> body != null && body.statusSetAs, callback);
    }

    /** Unread Info + personal notifications (0 when signed out). Observed by the header's bell badge. */
    public LiveData<Integer> unreadCount() {
        return unreadCount;
    }

    /**
     * Reloads {@link #unreadCount()} from the server: the sum of the unread "Info" and personal counts,
     * like the Dart HeaderModel.getJumlahNotifikasi(). Signed out: 0 without a request.
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
        enqueue(api.getJumlahNotifikasi("unread"),
                body -> parts[0] = body == null ? 0 : body.jumlahNotifikasi, done);
        enqueue(api.getJumlahNotifikasiKhusus("unread"),
                body -> parts[1] = body == null ? 0 : body.jumlahNotifikasi, done);
    }

    private static <T> List<T> nonNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
