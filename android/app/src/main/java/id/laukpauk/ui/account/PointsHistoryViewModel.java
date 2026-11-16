package id.laukpauk.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.PointRecords;
import id.laukpauk.data.repository.UserRepository;

/** HistoriPoin: the points history. */
public class PointsHistoryViewModel extends ViewModel {

    private final UserRepository repository;
    private final MediatorLiveData<Resource<List<PointRecords>>> history = new MediatorLiveData<>();
    private LiveData<Resource<List<PointRecords>>> source;

    public PointsHistoryViewModel(UserRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<PointRecords>>> getHistory() {
        return history;
    }

    /** First open and pull-to-refresh. */
    public void load() {
        if (source != null) {
            history.removeSource(source);
        }
        source = repository.getPointRecords();
        history.addSource(source, history::setValue);
    }
}
