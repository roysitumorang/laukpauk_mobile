package id.laukpauk.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Others;
import id.laukpauk.data.model.product.Pack;
import id.laukpauk.data.repository.PackRepository;

/** State of DetailPaket: the paket and "Paket Lainnya". */
public class PackDetailViewModel extends ViewModel {

    private final PackRepository repository;
    private final String packId;
    private final MediatorLiveData<Resource<Pack>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Others>>> others = new MediatorLiveData<>();
    private LiveData<Resource<Pack>> detailSource;
    private LiveData<Resource<List<Others>>> othersSource;

    public PackDetailViewModel(PackRepository repository, String packId) {
        this.repository = repository;
        this.packId = packId;
        load();
    }

    public LiveData<Resource<Pack>> getDetail() {
        return detail;
    }

    public LiveData<Resource<List<Others>>> getOthers() {
        return others;
    }

    public void load() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
            others.removeSource(othersSource);
        }
        detailSource = repository.getPackDetail(packId);
        detail.addSource(detailSource, detail::setValue);
        othersSource = repository.getOtherPacks(packId);
        others.addSource(othersSource, others::setValue);
    }
}
