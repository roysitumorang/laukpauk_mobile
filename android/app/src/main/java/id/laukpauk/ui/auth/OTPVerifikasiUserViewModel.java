package id.laukpauk.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.repository.AuthRepository;
import id.laukpauk.data.repository.BaseRepository.ResultCallback;

/** State of OTPVerifikasiUser: the busy flag and the code length. */
public class OTPVerifikasiUserViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);

    public OTPVerifikasiUserViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    public boolean isFormComplete(int codeLength) {
        return codeLength >= 4;
    }

    public void verify(String code, ResultCallback<Boolean> cb) {
        busy.setValue(true);
        repository.verifikasiUser(code, r -> {
            busy.setValue(false);
            cb.onResult(r);
        });
    }
}
