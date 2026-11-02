package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.orders.PesananMasuk;
import id.laukpauk.data.repository.PesananMasukRepository;

/** PesananMasuk state: the selected status tab (0 baru, 1 otw, 2 received, 3 rejected), date range and list. */
public class PesananMasukViewModel extends ViewModel {

    private static final String[] STATUS = {"processing", "otw", "received", "rejected"};

    private final PesananMasukRepository repository;
    private final MutableLiveData<Integer> tab;
    private final MediatorLiveData<Resource<List<PesananMasuk>>> list = new MediatorLiveData<>();
    private LiveData<Resource<List<PesananMasuk>>> source;
    /** Legacy: collected by the date fields but never sent to the server. */
    public String tanggalAwal = "";
    public String tanggalAkhir = "";

    public PesananMasukViewModel(PesananMasukRepository repository, int initialTab) {
        this.repository = repository;
        this.tab = new MutableLiveData<>(initialTab);
    }

    public LiveData<Integer> getTab() {
        return tab;
    }

    public LiveData<Resource<List<PesananMasuk>>> getList() {
        return list;
    }

    public void selectTab(int value) {
        if (tab.getValue() == null || tab.getValue() != value) {
            tab.setValue(value);
            load();
        }
    }

    public boolean isLoaded() {
        return source != null;
    }

    public void load() {
        if (source != null) {
            list.removeSource(source);
        }
        int t = tab.getValue() == null ? 0 : tab.getValue();
        source = repository.getPesananMasuk(STATUS[Math.max(0, Math.min(3, t))]);
        list.addSource(source, list::setValue);
    }
}
