package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.seller.shop.DetailPenjual;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.TokoSayaRepository;

/** State of RequestPencairanDana.dart: balance and the prefilled account. */
public class RequestPencairanDanaViewModel extends ViewModel {

    private final TokoSayaRepository repository;
    private final MutableLiveData<Integer> saldo = new MutableLiveData<>(0);
    private LiveData<Resource<DetailPenjual>> rekening;
    private boolean started;
    /** The account fields are prefilled only once. */
    boolean prefilled;

    public RequestPencairanDanaViewModel(TokoSayaRepository repository) {
        this.repository = repository;
    }

    public LiveData<Integer> getSaldo() {
        return saldo;
    }

    public LiveData<Resource<DetailPenjual>> getRekening() {
        return rekening;
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        rekening = repository.getRekeningPenjual();
        LiveData<Resource<Integer>> s = repository.getTotalSaldo();
        s.observeForever(new androidx.lifecycle.Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> r) {
                if (!r.isLoading()) {
                    saldo.setValue(r.isSuccess() && r.data != null ? r.data : 0);
                    s.removeObserver(this);
                }
            }
        });
    }

    public void ajukan(String rekeningNo, String penerima, String nominal, String keterangan,
                       BaseRepository.ResultCallback<Boolean> callback) {
        repository.ajukanRequestPencairan(rekeningNo, penerima, nominal, keterangan, callback);
    }
}
