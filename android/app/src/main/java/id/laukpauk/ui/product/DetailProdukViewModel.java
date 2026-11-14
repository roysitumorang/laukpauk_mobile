package id.laukpauk.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Lainnya;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.data.repository.ProdukRepository;

/** State of DetailProduk: the product and "Produk Lainnya". */
public class DetailProdukViewModel extends ViewModel {

    private final ProdukRepository repository;
    private final String idProduk;
    private final MediatorLiveData<Resource<Produk>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Lainnya>>> lainnya = new MediatorLiveData<>();
    private LiveData<Resource<Produk>> detailSource;
    private LiveData<Resource<List<Lainnya>>> lainnyaSource;

    public DetailProdukViewModel(ProdukRepository repository, String idProduk) {
        this.repository = repository;
        this.idProduk = idProduk;
        load();
    }

    public LiveData<Resource<Produk>> getDetail() {
        return detail;
    }

    public LiveData<Resource<List<Lainnya>>> getLainnya() {
        return lainnya;
    }

    public void load() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
            lainnya.removeSource(lainnyaSource);
        }
        detailSource = repository.getDetailProduk(idProduk);
        detail.addSource(detailSource, detail::setValue);
        lainnyaSource = repository.getProdukLainnya(idProduk);
        lainnya.addSource(lainnyaSource, lainnya::setValue);
    }
}
