package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.orders.PesananMasukItem;
import id.laukpauk.data.model.seller.orders.TransaksiPenjual;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.PesananMasukRepository;

/** ProsesPesanan state: the order's status and (while it is "processing") its items. */
public class ProsesPesananViewModel extends ViewModel {

    private final PesananMasukRepository repository;
    private final String idTransaksi;
    private final MediatorLiveData<Resource<TransaksiPenjual>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<PesananMasukItem>>> items = new MediatorLiveData<>();
    private LiveData<Resource<TransaksiPenjual>> detailSource;
    private LiveData<Resource<List<PesananMasukItem>>> itemsSource;

    public ProsesPesananViewModel(PesananMasukRepository repository, String idTransaksi) {
        this.repository = repository;
        this.idTransaksi = idTransaksi;
    }

    public LiveData<Resource<TransaksiPenjual>> getDetail() {
        return detail;
    }

    public LiveData<Resource<List<PesananMasukItem>>> getItems() {
        return items;
    }

    public boolean isLoaded() {
        return detailSource != null;
    }

    public boolean itemsLoaded() {
        return itemsSource != null;
    }

    /** Reloads the order; the screen reloads the items when the order is still "processing". */
    public void loadDetail() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
        }
        detailSource = repository.getTransaksi(idTransaksi);
        detail.addSource(detailSource, detail::setValue);
    }

    public void loadItems() {
        if (itemsSource != null) {
            items.removeSource(itemsSource);
        }
        itemsSource = repository.getPesananMasukItem(idTransaksi);
        items.addSource(itemsSource, items::setValue);
    }

    public void prosesPesanan(BaseRepository.ResultCallback<Boolean> callback) {
        repository.prosesPesanan(idTransaksi, callback);
    }

    public void pembatalanKlaim(String idItem, BaseRepository.ResultCallback<Boolean> callback) {
        repository.pembatalanKlaimTidakAda(idItem, callback);
    }
}
