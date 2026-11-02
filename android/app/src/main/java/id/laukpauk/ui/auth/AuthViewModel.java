package id.laukpauk.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.model.auth.AuthResponses;
import id.laukpauk.data.repository.AuthRepository;
import id.laukpauk.data.repository.BaseRepository.ResultCallback;

/** Busy state + actions of the auth form screens (one instance per screen). */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<String> nomorHP = new MutableLiveData<>();

    public AuthViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    /** The signed-in user's phone number (KirimUlangOTP prefill). */
    public LiveData<String> getNomorHP() {
        return nomorHP;
    }

    public boolean isBusy() {
        return Boolean.TRUE.equals(busy.getValue());
    }

    public void loadNomorHP() {
        if (nomorHP.getValue() == null) {
            repository.getNomorHP(r -> {
                if (r.isSuccess() && r.data != null) {
                    nomorHP.setValue(r.data);
                }
            });
        }
    }

    private <T> ResultCallback<T> done(ResultCallback<T> cb) {
        busy.setValue(true);
        return r -> {
            busy.setValue(false);
            cb.onResult(r);
        };
    }

    public void login(String nomor, String password, ResultCallback<AuthRepository.LoginResult> cb) {
        repository.login(nomor, password, done(cb));
    }

    public void register(String nama, String nomor, String pw, String konfirmasi, String referal,
                         ResultCallback<AuthRepository.RegisterResult> cb) {
        repository.register(nama, nomor, pw, konfirmasi, referal, done(cb));
    }

    public void konfirmasiOTP(String otp, ResultCallback<Boolean> cb) {
        repository.konfirmasiOTP(otp, done(cb));
    }

    public void resendOTP(String nomor, ResultCallback<AuthResponses.ResendOTP> cb) {
        repository.resendOTP(nomor, done(cb));
    }

    public void lupaPassword(String nomor, ResultCallback<AuthResponses.LupaPassword> cb) {
        repository.lupaPassword(nomor, done(cb));
    }
}
