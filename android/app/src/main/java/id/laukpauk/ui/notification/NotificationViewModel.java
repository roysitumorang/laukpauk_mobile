package id.laukpauk.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.notification.Notification;
import id.laukpauk.data.model.notification.SpecialNotification;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.NotificationRepository;

/** State of the Notifikasi screen: the selected tab and the two lists. Survives rotation. */
public class NotificationViewModel extends ViewModel {

    private final NotificationRepository repository;

    private final MutableLiveData<String> type = new MutableLiveData<>(NotificationRepository.INFO_TYPE);
    private final MediatorLiveData<Resource<List<Notification>>> info = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<SpecialNotification>>> special = new MediatorLiveData<>();
    private LiveData<Resource<List<Notification>>> infoSource;
    private LiveData<Resource<List<SpecialNotification>>> specialSource;

    public NotificationViewModel(NotificationRepository repository) {
        this.repository = repository;
    }

    /** "update" (Info) or "khususuntukkamu" (personal). */
    public LiveData<String> getType() {
        return type;
    }

    public LiveData<Resource<List<Notification>>> getInfo() {
        return info;
    }

    public LiveData<Resource<List<SpecialNotification>>> getSpecial() {
        return special;
    }

    public void selectType(String value) {
        if (!value.equals(type.getValue())) {
            type.setValue(value);
            load();
        }
    }

    /** Loads the selected list (first open, tab change, pull-to-refresh) and the unread counter. */
    public void load() {
        repository.refreshUnreadCount();
        if (NotificationRepository.INFO_TYPE.equals(type.getValue())) {
            if (infoSource != null) {
                info.removeSource(infoSource);
            }
            infoSource = repository.getNotificationInfo();
            info.addSource(infoSource, info::setValue);
        } else {
            if (specialSource != null) {
                special.removeSource(specialSource);
            }
            specialSource = repository.getSpecialNotification();
            special.addSource(specialSource, special::setValue);
        }
    }

    /**
     * Tapping a personal notification: an unread one is marked read first. On success the list and the
     * badge are reloaded and {@code callback} gets true (the screen then opens the notification's route).
     */
    public void open(SpecialNotification item, BaseRepository.ResultCallback<Boolean> callback) {
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
