package id.laukpauk.ui.account;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.Pasar;
import id.laukpauk.data.repository.AccountRepository;
import id.laukpauk.data.repository.BaseRepository;

/** State of the "Request Menjadi Penjual" form (the text fields keep their own state in the view). */
public class RequestJadiPenjualViewModel extends ViewModel {

    public static final String PASAR_TERSEDIA = "pasarTersedia";
    public static final String LOKASI_TERSENDIRI = "lokasiTersendiri";

    private final AccountRepository account;
    private final LiveData<Resource<List<Pasar>>> pasar;
    private final MutableLiveData<String> lokasiBerjualan = new MutableLiveData<>(PASAR_TERSEDIA);
    private final MutableLiveData<String> pasarSelected = new MutableLiveData<>("0");
    private final MutableLiveData<String[]> lokasiTersendiri = new MutableLiveData<>();
    private final MutableLiveData<String> pencarian = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);

    public RequestJadiPenjualViewModel(AccountRepository account) {
        this.account = account;
        this.pasar = account.getPasarTersedia();
    }

    public LiveData<Resource<List<Pasar>>> getPasar() {
        return pasar;
    }

    public LiveData<String> getLokasiBerjualan() {
        return lokasiBerjualan;
    }

    public LiveData<String> getPasarSelected() {
        return pasarSelected;
    }

    /** {lat, lng} chosen on the map, or null. */
    public LiveData<String[]> getLokasiTersendiri() {
        return lokasiTersendiri;
    }

    public LiveData<String> getPencarian() {
        return pencarian;
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    public void setLokasiBerjualan(String value) {
        lokasiBerjualan.setValue(value);
    }

    public void setPasarSelected(String id) {
        pasarSelected.setValue(id);
    }

    public void setLokasiTersendiri(String lat, String lng) {
        lokasiTersendiri.setValue(new String[]{lat, lng});
        lokasiBerjualan.setValue(LOKASI_TERSENDIRI);
    }

    public void setPencarian(String value) {
        pencarian.setValue(value);
    }

    public void ajukan(String namaToko, String alamatToko, String bio, String nomorRekening,
                       BaseRepository.ResultCallback<Boolean> callback) {
        String lokasi = lokasiBerjualan.getValue();
        String[] latLng = lokasiTersendiri.getValue();
        boolean tersendiri = LOKASI_TERSENDIRI.equalsIgnoreCase(lokasi) && latLng != null;
        busy.setValue(true);
        account.ajukanRequestPenjual(namaToko, alamatToko, bio, lokasi, pasarSelected.getValue(), nomorRekening,
                tersendiri ? latLng[0] : null, tersendiri ? latLng[1] : null, result -> {
                    busy.setValue(false);
                    callback.onResult(result);
                });
    }
}
