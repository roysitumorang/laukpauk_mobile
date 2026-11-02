package id.laukpauk.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.auth.AuthResponses;
import id.laukpauk.data.repository.AuthRepository;
import id.laukpauk.data.repository.BaseRepository.ResultCallback;

/** State of OTPVerifikasiUser: seller flag, market list with search, selection and the code length. */
public class OTPVerifikasiUserViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<Boolean> seller = new MutableLiveData<>(false);
    private final MutableLiveData<String> selected = new MutableLiveData<>("0");
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MediatorLiveData<Resource<List<AuthResponses.Pasar>>> pasar = new MediatorLiveData<>();
    private List<AuthResponses.Pasar> all = new ArrayList<>();
    private boolean loaded;

    public OTPVerifikasiUserViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> getSeller() {
        return seller;
    }

    public LiveData<String> getSelected() {
        return selected;
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    /** Markets filtered by the search text. */
    public LiveData<Resource<List<AuthResponses.Pasar>>> getPasar() {
        return pasar;
    }

    public String getQuery() {
        return query.getValue();
    }

    public void start() {
        if (loaded) {
            return;
        }
        loaded = true;
        repository.isSeller(r -> {
            if (r.isSuccess() && r.data != null) {
                seller.setValue(r.data);
            }
        });
        pasar.setValue(Resource.loading());
        pasar.addSource(repository.getPasarTersedia(), res -> {
            if (res.isSuccess()) {
                all = res.data == null ? new ArrayList<>() : res.data;
                filter();
            } else if (res.isError()) {
                all = new ArrayList<>();
                filter();
            }
        });
    }

    public void select(String idPasar) {
        selected.setValue(idPasar);
    }

    public void search(String text) {
        query.setValue(text == null ? "" : text);
        filter();
    }

    private void filter() {
        String q = query.getValue() == null ? "" : query.getValue().toLowerCase(Locale.ROOT);
        List<AuthResponses.Pasar> out = new ArrayList<>();
        for (AuthResponses.Pasar p : all) {
            if ((p.nama == null ? "" : p.nama).toLowerCase(Locale.ROOT).contains(q)) {
                out.add(p);
            }
        }
        pasar.setValue(Resource.success(out));
    }

    public boolean isFormComplete(int codeLength) {
        boolean s = Boolean.TRUE.equals(seller.getValue());
        return s ? (codeLength >= 4 && !"0".equals(selected.getValue())) : codeLength >= 4;
    }

    public void verify(String code, ResultCallback<Boolean> cb) {
        busy.setValue(true);
        repository.verifikasiUser(code, Boolean.TRUE.equals(seller.getValue()), selected.getValue(), r -> {
            busy.setValue(false);
            cb.onResult(r);
        });
    }
}
