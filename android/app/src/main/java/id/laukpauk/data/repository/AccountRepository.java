package id.laukpauk.data.repository;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.local.Prefs;
import id.laukpauk.data.model.account.AccountResponses;
import id.laukpauk.data.model.account.Pasar;
import id.laukpauk.data.remote.AccountApi;

/**
 * Account actions: logout, phone number change, referral settings, seller request. Profile / wallet data lives
 * in {@link UserRepository}.
 */
public class AccountRepository extends BaseRepository {

    private static final String KEY_ID_PENGGANTIAN = Prefs.KEY_PREFIX + "idPenggantianNomor";

    private final AccountApi api;
    private final CartRepository cart;
    private final NotifikasiRepository notifikasi;

    public AccountRepository(RepositoryDeps deps, CartRepository cart, NotifikasiRepository notifikasi) {
        super(deps);
        this.api = api(AccountApi.class);
        this.cart = cart;
        this.notifikasi = notifikasi;
    }

    // ------------------------------------------------------------------ logout

    /**
     * Legacy ProfilSayaActivity.logout: Auth/removeToken, then the stored token / OneSignal / external id are
     * cleared, the transaction session is removed and the badges reset. Callback gets true when logged out.
     */
    public void logout(ResultCallback<Boolean> callback) {
        enqueue(api.removeToken(authToken(), session.getCartCode()),
                (AccountResponses.RemoveToken body) -> body != null && body.removeToken, result -> {
                    if (!(result.isSuccess() && Boolean.TRUE.equals(result.data))) {
                        callback.onResult(result.isSuccess() ? Resource.success(false) : result);
                        return;
                    }
                    prefs.remove("authToken");
                    OneSignal.logout();
                    session.removeExternalUserID();
                    cart.clearTransactionSession(done -> {
                        cart.refreshCartCount();
                        notifikasi.refreshUnreadCount();
                        callback.onResult(Resource.success(true));
                    });
                });
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
        enqueue(api.jumlahGantiNomorHPForToday(authToken()),
                (AccountResponses.JumlahGantiNomor body) -> body == null ? 0 : body.jumlahGantiNomorHPForToday,
                callback);
    }

    /** Auth/gantiNomor; on success the returned id is stored and the callback gets it (0 = failed). */
    public void simpanNomorBaru(String nomorBaru, ResultCallback<Long> callback) {
        enqueue(api.gantiNomor(authToken(), nomorBaru), (AccountResponses.GantiNomor body) -> {
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
        enqueue(api.verifikasiNomor(authToken(), id, kodeVerifikasi), (AccountResponses.VerifikasiNomor body) -> {
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
        return load(api.getReferralConfiguration(authToken()),
                (AccountResponses.ReferralConfiguration body) ->
                        body == null || body.referralConfiguration == null ? null : body.referralConfiguration.referralWallet);
    }

    // ------------------------------------------------------------------ seller request

    public LiveData<Resource<List<Pasar>>> getPasarTersedia() {
        return load(api.getPasar("pasarTersedia"), (AccountResponses.PasarList body) ->
                body == null || body.dataPasar == null ? new ArrayList<Pasar>() : body.dataPasar);
    }

    /**
     * Penjual/ajukanRequestPenjual. {@code lat}/{@code lng} only for "lokasiTersendiri". Callback gets true when
     * the server accepted the request; an error result means the request did not answer 200.
     */
    public void ajukanRequestPenjual(String namaToko, String alamatToko, String bio, String lokasiBerjualan,
                                     String pasar, String nomorRekening, @Nullable String lat, @Nullable String lng,
                                     ResultCallback<Boolean> callback) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("namaToko", namaToko);
        fields.put("alamatToko", alamatToko);
        fields.put("bio", bio);
        fields.put("lokasiBerjualan", lokasiBerjualan);
        fields.put("pasar", pasar);
        fields.put("nomorRekening", nomorRekening);
        if (lat != null && lng != null) {
            fields.put("lat", lat);
            fields.put("long", lng);
        }
        enqueue(api.ajukanRequestPenjual(authToken(), fields),
                (AccountResponses.RequestPenjual body) -> body != null && body.statusRequest, callback);
    }
}
