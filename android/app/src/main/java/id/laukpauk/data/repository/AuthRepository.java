package id.laukpauk.data.repository;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.auth.AuthResponses;
import id.laukpauk.data.remote.AuthApi;

/**
 * Login, registration, OTP confirmation, user verification and password recovery. A successful login /
 * OTP confirmation stores the session (as legacy did), registers the device for push notifications and refreshes the
 * cart and notification badges.
 */
public class AuthRepository extends BaseRepository {

    /** Outcome of {@link #login}. */
    public static final class LoginResult {
        /** false: wrong phone/password combination. */
        public final boolean authenticated;
        public final boolean verified;

        LoginResult(boolean authenticated, boolean verified) {
            this.authenticated = authenticated;
            this.verified = verified;
        }
    }

    /** Outcome of {@link #register}. */
    public static final class RegisterResult {
        public final boolean success;
        /** true when the server explicitly answered statusRegister=false. */
        public final boolean failed;
        public final String message;

        RegisterResult(boolean success, boolean failed, String message) {
            this.success = success;
            this.failed = failed;
            this.message = message;
        }
    }

    private final AuthApi api;
    private final CartRepository cart;
    private final NotifikasiRepository notifikasi;
    private final PushRepository push;

    public AuthRepository(RepositoryDeps deps, CartRepository cart, NotifikasiRepository notifikasi,
                          PushRepository push) {
        super(deps);
        this.api = api(AuthApi.class);
        this.cart = cart;
        this.notifikasi = notifikasi;
        this.push = push;
    }

    /** Auth/loginAuthentication; on success the session is saved and the badges refreshed. */
    public void login(String nomorHP, String password, ResultCallback<LoginResult> callback) {
        enqueue(api.login(nomorHP, password, session.getCartCode()), body -> {
            if (body == null || !body.loginAuthentication) {
                return new LoginResult(false, false);
            }
            prefs.setString("authToken", nz(body.authToken));
            prefs.setString("idUser", nz(body.idUser));
            onSignedIn(nz(body.idUser));
            return new LoginResult(true, body.isVerified);
        }, callback);
    }

    public void register(String nama, String nomorHP, String password, String konfirmasi, String kodeReferal,
                         ResultCallback<RegisterResult> callback) {
        enqueue(api.register(nama, nomorHP, password, konfirmasi, kodeReferal), body -> {
            if (body == null) {
                return new RegisterResult(false, true, "");
            }
            String message = body.returnData == null ? "" : nz(body.returnData.message);
            boolean success = Boolean.TRUE.equals(body.statusRegister);
            if (success) {
                prefs.setBool("otpHasSent", true);
                prefs.setBool("otpHasConfirmed", false);
                prefs.setString("idUser", body.returnData == null ? "" : nz(body.returnData.idUser));
            }
            return new RegisterResult(success, Boolean.FALSE.equals(body.statusRegister), message);
        }, callback);
    }

    /** Auth/konfirmasiOTP; true when confirmed (the returned auth token becomes the session). */
    public void konfirmasiOTP(String otp, ResultCallback<Boolean> callback) {
        enqueue(api.konfirmasiOTP(session.getCurrentUserID(), otp), body -> {
            if (body == null || !body.statusKonfirmasiOTP) {
                return false;
            }
            prefs.setBool("otpHasConfirmed", true);
            prefs.setString("authToken", body.returnData == null ? "" : nz(body.returnData.authToken));
            onSignedIn(session.getCurrentUserID());
            return true;
        }, callback);
    }

    /** Auth/verifikasiUser; {@code idPasar} is only sent for sellers. */
    public void verifikasiUser(String code, boolean seller, String idPasar, ResultCallback<Boolean> callback) {
        enqueue(api.verifikasiUser(session.getCurrentUserID(), code, seller ? "yes" : "no",
                seller ? idPasar : null), body -> body != null && body.statusVerifikasiUser, callback);
    }

    public void resendOTP(String nomorTelepon, ResultCallback<AuthResponses.ResendOTP> callback) {
        enqueue(api.resendOTP(authToken(), nomorTelepon), body -> body, callback);
    }

    public void lupaPassword(String nomorHP, ResultCallback<AuthResponses.LupaPassword> callback) {
        enqueue(api.lupaPassword(nomorHP), body -> body, callback);
    }

    /** The signed-in user's phone number (null when unknown). */
    public void getNomorHP(ResultCallback<String> callback) {
        enqueue(api.getDetailUser(authToken(), null),
                body -> body == null || body.detailUser == null ? null : body.detailUser.nomorHP, callback);
    }

    /** Whether the user is a seller of the previous app version (needs to choose a market). */
    public void isSeller(ResultCallback<Boolean> callback) {
        enqueue(api.getDetailUser(authToken(), "isSeller"),
                body -> body != null && body.detailUser != null && "1".equals(body.detailUser.isSeller), callback);
    }

    /** The markets a seller can pick as the place of business. */
    public androidx.lifecycle.LiveData<Resource<List<AuthResponses.Pasar>>> getPasarTersedia() {
        return load(api.getPasar("pasarTersedia"), body -> {
            if (body == null || body.dataPasar == null) {
                return Collections.<AuthResponses.Pasar>emptyList();
            }
            return new ArrayList<>(body.dataPasar);
        });
    }

    private void onSignedIn(String idUser) {
        push.onSignedIn();
        cart.refreshCartCount();
        notifikasi.refreshUnreadCount();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
