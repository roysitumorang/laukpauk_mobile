package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.AccountResponses;
import id.laukpauk.data.model.account.WalletRecords;
import id.laukpauk.data.model.account.PointRecords;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.data.remote.UserApi;

/** The signed-in user's profile, wallet, vouchers and points (legacy UserModel / ProfilSayaModel / PointModel). */
public class UserRepository extends BaseRepository {

    public static final String WALLET_ALL = null;
    public static final String WALLET_REFERRAL = "referral";
    public static final String WALLET_TRANSACTION_DEDUCTION = "potongantransaksi";

    private final UserApi api;
    private final VoucherRepository vouchers;

    public UserRepository(RepositoryDeps deps, VoucherRepository vouchers) {
        super(deps);
        this.api = api(UserApi.class);
        this.vouchers = vouchers;
    }

    /** The signed-in buyer ({@code GET buyer/users/me}); data is null on an empty answer. */
    public LiveData<Resource<UserDetail>> getDetailUser() {
        return load(api.me(), UserRepository::me);
    }

    public void getDetailUser(ResultCallback<UserDetail> callback) {
        enqueue(api.me(), UserRepository::me, callback);
    }

    /** The wallet balance: {@code wallet} of {@code GET buyer/users/me}. */
    public LiveData<Resource<Long>> getWalletTotal() {
        return load(api.me(), (AccountResponses.Me body) -> {
            UserDetail user = me(body);
            return user == null ? 0L : user.walletOrZero();
        });
    }

    @Nullable
    static UserDetail me(@Nullable AccountResponses.Me body) {
        return body == null ? null : body.data;
    }

    /** How many vouchers the buyer has for the cart grand total (see {@link VoucherRepository#countForCart}). */
    public LiveData<Resource<Integer>> getVoucherCount() {
        MutableLiveData<Resource<Integer>> live = new MutableLiveData<>(Resource.loading());
        vouchers.countForCart(r -> live.setValue(r.isSuccess()
                ? Resource.success(r.data == null ? 0 : r.data.intValue())
                : Resource.<Integer>error(r.message, r.code)));
        return live;
    }

    /** The buyer's points: {@code reward_points} of {@code GET buyer/users/me}. */
    public LiveData<Resource<Long>> getPoint() {
        return load(api.me(), (AccountResponses.Me body) -> {
            UserDetail user = me(body);
            return user == null ? 0L : user.rewardPointsOrZero();
        });
    }

    /** {@code jenis}: WALLET_ALL (null), WALLET_REFERRAL or WALLET_TRANSACTION_DEDUCTION. */
    public LiveData<Resource<List<WalletRecords>>> getWalletRecords(@Nullable String type) {
        return load(api.getWalletRecords(type),
                (AccountResponses.WalletRecordList body) ->
                        body == null || body.walletRecords == null ? new ArrayList<WalletRecords>() : body.walletRecords);
    }

    public LiveData<Resource<List<PointRecords>>> getPointRecords() {
        return load(api.getPointRecords(),
                (AccountResponses.PointRecordList body) ->
                        body == null || body.pointRecords == null ? new ArrayList<PointRecords>() : body.pointRecords);
    }

    /** POST User/updateProfil; true when the server saved the changes. */
    public void updateProfile(String fullName, String address, String username, ResultCallback<Boolean> callback) {
        enqueue(api.updateProfile(body("namaLengkap", fullName, "alamat", address, "username", username)),
                (AccountResponses.UpdateProfile body) -> body != null && body.updateProfileStatus, callback);
    }

    /** POST User/updateFotoProfil with the image as base64 (encoded on the disk thread). */
    public void updateProfilePhoto(java.io.File image, ResultCallback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            String base64 = encode(image);
            executors.mainThread().execute(() -> enqueue(api.updateProfilePhoto(body("fotoProfil", base64)),
                    (AccountResponses.UpdatePhoto body) -> body != null && body.saveProfilePhoto, callback));
        });
    }

    /** POST User/gantiPassword; true when changed. */
    public void changePassword(String oldPassword, String newPassword, String confirm, ResultCallback<Boolean> callback) {
        enqueue(api.changePassword(body("passwordLama", oldPassword, "passwordBaru", newPassword, "konfirmasiPasswordBaru",
                confirm)),
                (AccountResponses.ChangePassword body) -> body != null && body.changePasswordStatus, callback);
    }

    private static String encode(java.io.File file) {
        try (java.io.FileInputStream in = new java.io.FileInputStream(file)) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP);
        } catch (java.io.IOException e) {
            return "";
        }
    }
}
