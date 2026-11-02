package id.laukpauk.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.RiwayatDompet;
import id.laukpauk.data.repository.UserRepository;

/** HistoriDompet: selected tab (0 = semua, 1 = referral, 2 = potongan transaksi) and its history. */
public class HistoriDompetViewModel extends ViewModel {

    private final UserRepository repository;
    private final MutableLiveData<Integer> jenis = new MutableLiveData<>(0);
    private final MediatorLiveData<Resource<List<RiwayatDompet>>> histori = new MediatorLiveData<>();
    private LiveData<Resource<List<RiwayatDompet>>> source;
    private boolean loaded;

    public HistoriDompetViewModel(UserRepository repository, int initialJenis) {
        this.repository = repository;
        jenis.setValue(initialJenis);
    }

    public LiveData<Integer> getJenis() {
        return jenis;
    }

    public LiveData<Resource<List<RiwayatDompet>>> getHistori() {
        return histori;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void selectJenis(int value) {
        Integer current = jenis.getValue();
        if (current == null || current != value) {
            jenis.setValue(value);
            load();
        }
    }

    public void load() {
        loaded = true;
        if (source != null) {
            histori.removeSource(source);
        }
        Integer j = jenis.getValue();
        String param = j == null ? null : j == 1 ? UserRepository.DOMPET_REFERRAL
                : j == 2 ? UserRepository.DOMPET_POTONGAN_TRANSAKSI : UserRepository.DOMPET_SEMUA;
        source = repository.getRiwayatDompet(param);
        histori.addSource(source, histori::setValue);
    }
}
