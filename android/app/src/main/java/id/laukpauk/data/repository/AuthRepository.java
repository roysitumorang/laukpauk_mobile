package id.laukpauk.data.repository;



import java.util.Map;
import id.laukpauk.data.Resource;
import id.laukpauk.util.Json;
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

    /**
     * POST users/sign_in; on success the session (JWT, its expiry and the current user) is saved and the
     * badges refreshed. A 4xx answer means the login or password is wrong.
     */
    public void login(String login, String password, ResultCallback<LoginResult> callback) {
        enqueue(api.signIn(body("login", login, "password", password)), body -> {
            // The backend wraps the payload in "data"; a bare payload is accepted too.
            Map<String, Object> payload = Json.isObject(Json.get(body, "data")) ? Json.object(body, "data") : body;
            String idToken = Json.str(payload, "id_token", "");
            Map<String, Object> user = Json.objectOrNull(payload, "user");
            if (idToken.isEmpty() || user == null) {
                return new LoginResult(false, false);
            }
            session.saveSignIn(idToken, Json.strOrNull(payload, "expired_at"), user);
            onSignedIn(session.getCurrentUserID());
            return new LoginResult(true, Json.get(user, "mobile_phone_confirmed_at") != null);
        }, result -> {
            boolean rejected = result.isError() && result.code >= 400 && result.code < 500;
            callback.onResult(rejected ? Resource.success(new LoginResult(false, false)) : result);
        });
    }

    public void register(String nama, String nomorHP, String password, String konfirmasi, String kodeReferal,
                         ResultCallback<RegisterResult> callback) {
        enqueue(api.register(body("namaLengkap", nama, "nomorHP", nomorHP, "password", password, "konfirmasiPassword",
                konfirmasi, "kodeReferal", kodeReferal)), body -> {
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
        enqueue(api.konfirmasiOTP(body("idUser", session.getCurrentUserID(), "otp", otp)), body -> {
            if (body == null || !body.statusKonfirmasiOTP) {
                return false;
            }
            prefs.setBool("otpHasConfirmed", true);
            session.clearSignIn();
            prefs.setString("authToken", body.returnData == null ? "" : nz(body.returnData.authToken));
            onSignedIn(session.getCurrentUserID());
            return true;
        }, callback);
    }

    /** Auth/verifikasiUser. */
    public void verifikasiUser(String code, ResultCallback<Boolean> callback) {
        enqueue(api.verifikasiUser(body("idUser", session.getCurrentUserID(), "verificationCode", code)),
                body -> body != null && body.statusVerifikasiUser, callback);
    }

    public void resendOTP(String nomorTelepon, ResultCallback<AuthResponses.ResendOTP> callback) {
        enqueue(api.resendOTP(body("nomorTelepon", nomorTelepon)), body -> body, callback);
    }

    public void lupaPassword(String nomorHP, ResultCallback<AuthResponses.LupaPassword> callback) {
        enqueue(api.lupaPassword(body("nomorHP", nomorHP)), body -> body, callback);
    }

    /** The signed-in user's phone number (null when unknown). */
    public void getNomorHP(ResultCallback<String> callback) {
        enqueue(api.getDetailUser(null),
                body -> body == null || body.detailUser == null ? null : body.detailUser.nomorHP, callback);
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
