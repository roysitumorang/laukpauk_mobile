package id.laukpauk.ui.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.data.repository.UserRepository;
import id.laukpauk.util.Json;

/** State of the "Akun" tab: the user, the wallet / voucher / point summary and the referral value. */
public class UserAndDashboardViewModel extends ViewModel {

    private final UserRepository users;
    private final SettingRepository settings;

    private final MediatorLiveData<Resource<UserDetail>> user = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Long>> wallet = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Integer>> voucher = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<Long>> points = new MediatorLiveData<>();
    private final MutableLiveData<Integer> referralWallet = new MutableLiveData<>();
    private LiveData<Resource<UserDetail>> userSource;
    private LiveData<Resource<Long>> walletSource;
    private LiveData<Resource<Integer>> voucherSource;
    private LiveData<Resource<Long>> pointsSource;

    public UserAndDashboardViewModel(UserRepository users, SettingRepository settings) {
        this.users = users;
        this.settings = settings;
    }

    public LiveData<Resource<UserDetail>> getUser() {
        return user;
    }

    public LiveData<Resource<Long>> getWallet() {
        return wallet;
    }

    public LiveData<Resource<Integer>> getVoucher() {
        return voucher;
    }

    public LiveData<Resource<Long>> getPoints() {
        return points;
    }

    /** The "referralWallet" setting (0 when missing); null while loading. */
    public LiveData<Integer> getReferralWallet() {
        return referralWallet;
    }

    /** First open, pull-to-refresh and every re-entry of the tab. */
    public void load() {
        if (userSource != null) {
            user.removeSource(userSource);
            wallet.removeSource(walletSource);
            voucher.removeSource(voucherSource);
            points.removeSource(pointsSource);
        }
        userSource = users.getDetailUser();
        user.addSource(userSource, user::setValue);
        walletSource = users.getWalletTotal();
        wallet.addSource(walletSource, wallet::setValue);
        voucherSource = users.getVoucherCount();
        voucher.addSource(voucherSource, voucher::setValue);
        pointsSource = users.getPoint();
        points.addSource(pointsSource, points::setValue);
        settings.getSettingValue("referralWallet", result -> referralWallet.setValue(
                result.isSuccess() && result.data != null ? Json.parseInt(Json.asStr(result.data, ""), 0) : 0));
    }

    /** The privacy policy URL ("" when missing). */
    public void privacyPolicyUrl(BaseRepository.ResultCallback<String> callback) {
        settings.getSettingValue("kebijakanPrivasiURL", result -> callback.onResult(Resource.success(
                result.isSuccess() && result.data != null ? Json.asStr(result.data, "") : "")));
    }
}
