package id.laukpauk.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.notifikasi.Notifikasi;
import id.laukpauk.data.repository.NotifikasiRepository;

/** Loads one "Info" notification once; kept across rotation. */
public class DetailNotifikasiGlobalViewModel extends ViewModel {

    private final NotifikasiRepository repository;
    private LiveData<Resource<Notifikasi>> detail;

    public DetailNotifikasiGlobalViewModel(NotifikasiRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Notifikasi>> getDetail(String idNotifikasi) {
        if (detail == null) {
            detail = repository.getDetailNotifikasi(idNotifikasi);
        }
        return detail;
    }
}
