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
    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();

    public AuthViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    /** The signed-in user's phone number (KirimUlangOTP prefill). */
    public LiveData<String> getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isBusy() {
        return Boolean.TRUE.equals(busy.getValue());
    }

    public void loadPhoneNumber() {
        if (phoneNumber.getValue() == null) {
            repository.getPhoneNumber(r -> {
                if (r.isSuccess() && r.data != null) {
                    phoneNumber.setValue(r.data);
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

    public void login(String number, String password, ResultCallback<AuthRepository.LoginResult> cb) {
        repository.login(number, password, done(cb));
    }

    public void register(String name, String username, String number, String pw, String referral,
                         ResultCallback<AuthRepository.RegisterResult> cb) {
        repository.register(name, username, number, pw, referral, done(cb));
    }

    public void confirmOtp(String otp, ResultCallback<Boolean> cb) {
        repository.confirmOtp(otp, done(cb));
    }

    public void resendOTP(String number, ResultCallback<AuthResponses.ResendOTP> cb) {
        repository.resendOTP(number, done(cb));
    }

    public void forgotPassword(String number, ResultCallback<AuthResponses.ForgotPassword> cb) {
        repository.forgotPassword(number, done(cb));
    }
}
