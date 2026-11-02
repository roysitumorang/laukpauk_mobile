package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.products.ProdukPenjual;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.ProdukPenjualRepository;

/** ListProdukPenjual state: the selected tab (0 = satuan, 1 = paket) and the two product lists. */
public class ListProdukPenjualViewModel extends ViewModel {

    private final ProdukPenjualRepository repository;
    private final MutableLiveData<Integer> tab = new MutableLiveData<>(0);
    private final MediatorLiveData<Resource<List<ProdukPenjual>>> satuan = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<ProdukPenjual>>> paket = new MediatorLiveData<>();
    private LiveData<Resource<List<ProdukPenjual>>> satuanSource;
    private LiveData<Resource<List<ProdukPenjual>>> paketSource;

    public ListProdukPenjualViewModel(ProdukPenjualRepository repository) {
        this.repository = repository;
    }

    public LiveData<Integer> getTab() {
        return tab;
    }

    public LiveData<Resource<List<ProdukPenjual>>> getSatuan() {
        return satuan;
    }

    public LiveData<Resource<List<ProdukPenjual>>> getPaket() {
        return paket;
    }

    public void selectTab(int value) {
        if (tab.getValue() == null || tab.getValue() != value) {
            tab.setValue(value);
            load();
        }
    }

    public boolean isLoaded() {
        return satuan.getValue() != null || paket.getValue() != null;
    }

    /** Loads the selected tab's list (first open, tab change, refresh, after a change). */
    public void load() {
        if (tab.getValue() != null && tab.getValue() == 1) {
            if (paketSource != null) {
                paket.removeSource(paketSource);
            }
            paketSource = repository.getPaketPenjual();
            paket.addSource(paketSource, paket::setValue);
        } else {
            if (satuanSource != null) {
                satuan.removeSource(satuanSource);
            }
            satuanSource = repository.getProdukPenjual();
            satuan.addSource(satuanSource, satuan::setValue);
        }
    }

    public void hapus(String id, boolean isPaket, BaseRepository.ResultCallback<Boolean> callback) {
        if (isPaket) {
            repository.hapusPaket(id, callback);
        } else {
            repository.hapusProduk(id, callback);
        }
    }
}
