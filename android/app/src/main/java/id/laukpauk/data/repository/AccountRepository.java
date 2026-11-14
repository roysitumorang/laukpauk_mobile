package id.laukpauk.data.repository;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;



import id.laukpauk.data.Resource;
import id.laukpauk.data.local.Prefs;
import id.laukpauk.data.model.account.AccountResponses;
import id.laukpauk.data.remote.AccountApi;

/**
 * Account actions: logout, phone number change, referral settings. Profile / wallet data lives
 * in {@link UserRepository}.
 */
public class AccountRepository extends BaseRepository {

    private static final String KEY_ID_PENGGANTIAN = Prefs.KEY_PREFIX + "idPenggantianNomor";

    private final AccountApi api;
    private final CartRepository cart;
    private final NotifikasiRepository notifikasi;
    private final PushRepository push;

    public AccountRepository(RepositoryDeps deps, CartRepository cart, NotifikasiRepository notifikasi,
                             PushRepository push) {
        super(deps);
        this.api = api(AccountApi.class);
        this.cart = cart;
        this.notifikasi = notifikasi;
        this.push = push;
    }

    // ------------------------------------------------------------------ logout

    /**
     * Legacy ProfilSayaActivity.logout: the push token is unregistered first (that call needs the still-valid
     * auth token), then Auth/removeToken; on success the stored token is cleared, the transaction session is
     * removed and the badges reset. Callback gets true when logged out. If removeToken fails the user stays
     * signed in and the next app start registers a fresh push token.
     */
    public void logout(ResultCallback<Boolean> callback) {
        push.onSigningOut(() -> enqueue(api.removeToken(session.getCartCode()),
                (AccountResponses.RemoveToken body) -> body != null && body.removeToken, result -> {
                    if (!(result.isSuccess() && Boolean.TRUE.equals(result.data))) {
                        callback.onResult(result.isSuccess() ? Resource.success(false) : result);
                        return;
                    }
                    prefs.remove("authToken");
                    cart.clearTransactionSession(done -> {
                        cart.refreshCartCount();
                        notifikasi.refreshUnreadCount();
                        callback.onResult(Resource.success(true));
                    });
                }));
    }

    // ------------------------------------------------------------------ phone number change

    private SharedPreferences sp() {
        return deps.application.getSharedPreferences(Prefs.FILE_NAME, android.content.Context.MODE_PRIVATE);
    }

    /** The id of a pending number change (0 = none). */
    public long getIdPenggantianNomor() {
        try {
            return sp().getLong(KEY_ID_PENGGANTIAN, 0L);
        } catch (ClassCastException e) {
            try {
                return sp().getInt(KEY_ID_PENGGANTIAN, 0);
            } catch (ClassCastException e2) {
                return 0L;
            }
        }
    }

    public void clearIdPenggantianNomor() {
        prefs.remove("idPenggantianNomor");
    }

    /** Auth/jumlahGantiNomorHPForToday. */
    public void jumlahGantiNomorHPForToday(ResultCallback<Integer> callback) {
        enqueue(api.jumlahGantiNomorHPForToday(),
                (AccountResponses.JumlahGantiNomor body) -> body == null ? 0 : body.jumlahGantiNomorHPForToday,
                callback);
    }

    /** Auth/gantiNomor; on success the returned id is stored and the callback gets it (0 = failed). */
    public void simpanNomorBaru(String nomorBaru, ResultCallback<Long> callback) {
        enqueue(api.gantiNomor(body("nomorBaru", nomorBaru)), (AccountResponses.GantiNomor body) -> {
            if (body != null && body.statusSimpanNomorBaru) {
                sp().edit().putLong(KEY_ID_PENGGANTIAN, body.idPenggantianNomor).apply();
                return body.idPenggantianNomor;
            }
            return 0L;
        }, callback);
    }

    /** Auth/verifikasiNomor; true when verified (the pending id is cleared). */
    public void verifikasiNomor(String kodeVerifikasi, ResultCallback<Boolean> callback) {
        String id = sp().contains(KEY_ID_PENGGANTIAN) ? String.valueOf(getIdPenggantianNomor()) : "null";
        enqueue(api.verifikasiNomor(body("idPenggantianNomor", id, "kodeVerifikasi", kodeVerifikasi)),
                (AccountResponses.VerifikasiNomor body) -> {
            boolean ok = body != null && body.statusVerifikasiNomor;
            if (ok) {
                clearIdPenggantianNomor();
            }
            return ok;
        }, callback);
    }

    // ------------------------------------------------------------------ referral

    /** The referral wallet value; data is null when referral settings are missing ("belum dibuat"). */
    public LiveData<Resource<Long>> getReferralWallet() {
        return load(api.getReferralConfiguration(),
                (AccountResponses.ReferralConfiguration body) ->
                        body == null || body.referralConfiguration == null ? null : body.referralConfiguration.referralWallet);
    }

}
