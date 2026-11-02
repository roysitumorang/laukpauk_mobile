package id.laukpauk.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.RiwayatPoint;
import id.laukpauk.data.repository.UserRepository;

/** HistoriPoin: the points history. */
public class HistoriPoinViewModel extends ViewModel {

    private final UserRepository repository;
    private final MediatorLiveData<Resource<List<RiwayatPoint>>> histori = new MediatorLiveData<>();
    private LiveData<Resource<List<RiwayatPoint>>> source;

    public HistoriPoinViewModel(UserRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<RiwayatPoint>>> getHistori() {
        return histori;
    }

    /** First open and pull-to-refresh. */
    public void load() {
        if (source != null) {
            histori.removeSource(source);
        }
        source = repository.getRiwayatPoint();
        histori.addSource(source, histori::setValue);
    }
}
