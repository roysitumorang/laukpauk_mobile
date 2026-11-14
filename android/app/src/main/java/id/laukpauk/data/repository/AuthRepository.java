package id.laukpauk.data.repository;



import java.util.Map;
import id.laukpauk.data.Resource;
import id.laukpauk.util.Json;
import id.laukpauk.data.model.auth.AuthResponses;
import id.laukpauk.data.remote.AuthApi;
import okio.ByteString;

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
        /** true when the server refused the registration (a 4xx answer) or sent no user. */
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
     * The password is always sent base64-encoded (standard alphabet, of its UTF-8 bytes), never as typed.
     */
    public void login(String login, String password, ResultCallback<LoginResult> callback) {
        enqueue(api.signIn(body("login", login, "password", base64(password))), body -> {
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

    /**
     * POST users (buyer registration). On success the new user's id is kept for the OTP confirmation that
     * follows. The password is sent base64-encoded, like sign-in's; an empty referral code is left out.
     * A 4xx answer is a refusal whose reason is the server's message.
     */
    public void register(String name, String username, String mobilePhone, String password, String referralCode,
                         ResultCallback<RegisterResult> callback) {
        String referral = referralCode == null || referralCode.trim().isEmpty() ? null : referralCode.trim();
        enqueue(api.signUp(body("name", name, "username", username, "mobile_phone", mobilePhone,
                "password", base64(password), "referral_code", referral)), body -> {
            // The backend wraps the user in "data"; a bare user object is accepted too.
            Map<String, Object> user = Json.isObject(Json.get(body, "data")) ? Json.object(body, "data") : body;
            String idUser = Json.str(user, "id", "");
            if (idUser.isEmpty()) {
                return new RegisterResult(false, true, "");
            }
            prefs.setBool("otpHasSent", true);
            prefs.setBool("otpHasConfirmed", false);
            prefs.setString("idUser", idUser);
            return new RegisterResult(true, false, "");
        }, result -> {
            boolean rejected = result.isError() && result.code >= 400 && result.code < 500;
            callback.onResult(rejected
                    ? Resource.success(new RegisterResult(false, true, nz(result.message))) : result);
        });
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

    /** Standard base64 (with padding, no line breaks) of the text's UTF-8 bytes. */
    static String base64(String text) {
        return ByteString.encodeUtf8(text == null ? "" : text).base64();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
