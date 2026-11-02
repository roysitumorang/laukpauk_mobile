package id.laukpauk.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Lainnya;
import id.laukpauk.data.model.product.Paket;
import id.laukpauk.data.repository.PaketRepository;

/** State of DetailPaket: the paket and "Paket Lainnya". */
public class DetailPaketViewModel extends ViewModel {

    private final PaketRepository repository;
    private final String idPaket;
    private final MediatorLiveData<Resource<Paket>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Lainnya>>> lainnya = new MediatorLiveData<>();
    private LiveData<Resource<Paket>> detailSource;
    private LiveData<Resource<List<Lainnya>>> lainnyaSource;

    public DetailPaketViewModel(PaketRepository repository, String idPaket) {
        this.repository = repository;
        this.idPaket = idPaket;
        load();
    }

    public LiveData<Resource<Paket>> getDetail() {
        return detail;
    }

    public LiveData<Resource<List<Lainnya>>> getLainnya() {
        return lainnya;
    }

    public String currentUserId() {
        return repository.currentUserId();
    }

    public void load() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
            lainnya.removeSource(lainnyaSource);
        }
        detailSource = repository.getDetailPaket(idPaket);
        detail.addSource(detailSource, detail::setValue);
        lainnyaSource = repository.getPaketLainnya(idPaket);
        lainnya.addSource(lainnyaSource, lainnya::setValue);
    }
}
