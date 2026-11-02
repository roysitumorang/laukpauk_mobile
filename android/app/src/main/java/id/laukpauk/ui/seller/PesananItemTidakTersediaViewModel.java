package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.PesananMasukRepository;

/** Claim state of PesananItemTidakTersedia: the reason text and the "sending" guard. */
public class PesananItemTidakTersediaViewModel extends ViewModel {

    private final PesananMasukRepository repository;
    private final String idItem;
    private final MutableLiveData<Boolean> sending = new MutableLiveData<>(false);
    public String keterangan = "";

    public PesananItemTidakTersediaViewModel(PesananMasukRepository repository, String idItem) {
        this.repository = repository;
        this.idItem = idItem;
    }

    public LiveData<Boolean> getSending() {
        return sending;
    }

    public void klaim(BaseRepository.ResultCallback<Boolean> callback) {
        if (Boolean.TRUE.equals(sending.getValue())) {
            return;
        }
        sending.setValue(true);
        repository.klaimProdukTidakTersedia(idItem, keterangan, r -> {
            sending.setValue(false);
            callback.onResult(r);
        });
    }
}
