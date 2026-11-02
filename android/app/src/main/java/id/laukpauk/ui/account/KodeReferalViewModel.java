package id.laukpauk.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.data.repository.AccountRepository;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.data.repository.UserRepository;
import id.laukpauk.util.Json;

/** KodeReferal: referral configuration (wallet value) and the user's code. */
public class KodeReferalViewModel extends ViewModel {

    private final AccountRepository account;
    private final UserRepository users;
    private final SettingRepository settings;

    private final MediatorLiveData<Resource<Long>> config = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<UserDetail>> user = new MediatorLiveData<>();
    private LiveData<Resource<Long>> configSource;
    private LiveData<Resource<UserDetail>> userSource;
    private boolean loaded;

    public KodeReferalViewModel(AccountRepository account, UserRepository users, SettingRepository settings) {
        this.account = account;
        this.users = users;
        this.settings = settings;
    }

    /** Success with null data = referral not configured. */
    public LiveData<Resource<Long>> getConfig() {
        return config;
    }

    public LiveData<Resource<UserDetail>> getUser() {
        return user;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load() {
        loaded = true;
        if (configSource != null) {
            config.removeSource(configSource);
        }
        if (userSource != null) {
            user.removeSource(userSource);
        }
        configSource = account.getReferralWallet();
        config.addSource(configSource, config::setValue);
        userSource = users.getDetailUser();
        user.addSource(userSource, user::setValue);
    }

    /** The "referralUserWallet" setting (0 when missing), for the share text. */
    public void referralUserWallet(BaseRepository.ResultCallback<Long> callback) {
        settings.getSettingValue("referralUserWallet", result -> {
            long value = 0;
            if (result.isSuccess() && result.data != null) {
                value = Json.parseInt(Json.asStr(result.data, ""), 0);
            }
            callback.onResult(Resource.success(value));
        });
    }
}
