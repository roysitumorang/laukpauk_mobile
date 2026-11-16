package id.laukpauk.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.notification.Notification;
import id.laukpauk.data.repository.NotificationRepository;

/** Loads one "Info" notification once; kept across rotation. */
public class GlobalNotificationDetailViewModel extends ViewModel {

    private final NotificationRepository repository;
    private LiveData<Resource<Notification>> detail;

    public GlobalNotificationDetailViewModel(NotificationRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Notification>> getDetail(String notificationId) {
        if (detail == null) {
            detail = repository.getNotificationDetail(notificationId);
        }
        return detail;
    }
}
