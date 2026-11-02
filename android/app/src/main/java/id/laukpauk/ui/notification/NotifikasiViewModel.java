package id.laukpauk.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.notifikasi.Notifikasi;
import id.laukpauk.data.model.notifikasi.NotifikasiKhusus;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.NotifikasiRepository;

/** State of the Notifikasi screen: the selected tab and the two lists. Survives rotation. */
public class NotifikasiViewModel extends ViewModel {

    private final NotifikasiRepository repository;

    private final MutableLiveData<String> jenis = new MutableLiveData<>(NotifikasiRepository.JENIS_INFO);
    private final MediatorLiveData<Resource<List<Notifikasi>>> info = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<NotifikasiKhusus>>> khusus = new MediatorLiveData<>();
    private LiveData<Resource<List<Notifikasi>>> infoSource;
    private LiveData<Resource<List<NotifikasiKhusus>>> khususSource;

    public NotifikasiViewModel(NotifikasiRepository repository) {
        this.repository = repository;
    }

    /** "update" (Info) or "khususuntukkamu" (personal). */
    public LiveData<String> getJenis() {
        return jenis;
    }

    public LiveData<Resource<List<Notifikasi>>> getInfo() {
        return info;
    }

    public LiveData<Resource<List<NotifikasiKhusus>>> getKhusus() {
        return khusus;
    }

    public void selectJenis(String value) {
        if (!value.equals(jenis.getValue())) {
            jenis.setValue(value);
            load();
        }
    }

    /** Loads the selected list (first open, tab change, pull-to-refresh) and the unread counter. */
    public void load() {
        repository.refreshUnreadCount();
        if (NotifikasiRepository.JENIS_INFO.equals(jenis.getValue())) {
            if (infoSource != null) {
                info.removeSource(infoSource);
            }
            infoSource = repository.getNotifikasiInfo();
            info.addSource(infoSource, info::setValue);
        } else {
            if (khususSource != null) {
                khusus.removeSource(khususSource);
            }
            khususSource = repository.getNotifikasiKhusus();
            khusus.addSource(khususSource, khusus::setValue);
        }
    }

    /**
     * Tapping a personal notification: an unread one is marked read first. On success the list and the
     * badge are reloaded and {@code callback} gets true (the screen then opens the notification's route).
     */
    public void open(NotifikasiKhusus item, BaseRepository.ResultCallback<Boolean> callback) {
        if (item.isRead) {
            callback.onResult(Resource.success(true));
            return;
        }
        repository.setAsRead(item.id, result -> {
            if (result.isSuccess() && Boolean.TRUE.equals(result.data)) {
                load();
            }
            callback.onResult(result);
        });
    }
}
