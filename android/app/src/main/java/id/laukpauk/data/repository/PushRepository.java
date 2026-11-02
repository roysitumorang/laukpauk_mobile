package id.laukpauk.data.repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import id.laukpauk.BuildConfig;
import id.laukpauk.data.Resource;
import id.laukpauk.data.remote.PushApi;

/**
 * Push notification registration with Firebase Cloud Messaging (replaces OneSignal):
 * <ul>
 *   <li>every install subscribes to the broadcast topic {@code BuildConfig.FCM_BROADCAST_TOPIC} ("Info"
 *       notifications for everyone);</li>
 *   <li>a signed-in user's device registers its FCM token with the backend, which sends personal
 *       notifications to the user's tokens; sign-out unregisters it and deletes the token on the device.</li>
 * </ul>
 * Everything is a no-op when Firebase is not configured (see {@code FIREBASE_*} in laukpauk.properties).
 */
public class PushRepository extends BaseRepository {

    private static final String TAG = "PushRepository";
    /** Prefs key of the token last registered with the backend for the signed-in user. */
    private static final String KEY_REGISTERED_TOKEN = "fcmRegisteredToken";

    private final PushApi api;

    public PushRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(PushApi.class);
    }

    /** True when Firebase was initialised (AppStartup does it when the FIREBASE_* values are set). */
    public boolean isEnabled() {
        return !FirebaseApp.getApps(deps.application).isEmpty();
    }

    /**
     * App start: subscribe to the broadcast topic and (re-)register a signed-in user's token. Registering is
     * idempotent on the backend, so doing it on every start heals a lost registration.
     */
    public void start() {
        if (!isEnabled()) {
            return;
        }
        String topic = BuildConfig.FCM_BROADCAST_TOPIC;
        if (topic != null && !topic.isEmpty()) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnFailureListener(e -> Log.w(TAG, "subscribe " + topic, e));
        }
        if (!authToken().isEmpty()) {
            registerCurrentToken();
        }
    }

    /** After a successful sign-in / OTP confirmation. */
    public void onSignedIn() {
        if (isEnabled()) {
            registerCurrentToken();
        }
    }

    /** FirebaseMessagingService.onNewToken: the old token is invalid, register the new one when signed in. */
    public void onNewToken(String token) {
        prefs.remove(KEY_REGISTERED_TOKEN);
        if (!authToken().isEmpty()) {
            register(token);
        }
    }

    /**
     * Before the session is cleared on sign-out: unregisters the token on the backend (needs the auth token,
     * so call it first) and deletes it on the device, so this device stops receiving the user's personal
     * notifications even if the backend call fails. {@code done} always runs (main thread).
     */
    public void onSigningOut(Runnable done) {
        final String token = prefs.getString(KEY_REGISTERED_TOKEN);
        prefs.remove(KEY_REGISTERED_TOKEN);
        if (!isEnabled()) {
            done.run();
            return;
        }
        FirebaseMessaging.getInstance().deleteToken()
                .addOnFailureListener(e -> Log.w(TAG, "deleteToken", e));
        if (token == null || authToken().isEmpty()) {
            done.run();
            return;
        }
        enqueue(api.unregisterFcmToken(authToken(), token), body -> body != null && body.statusUnregister,
                (Resource<Boolean> r) -> {
                    if (!r.isSuccess()) {
                        Log.w(TAG, "unregisterFcmToken failed: " + r.message);
                    }
                    done.run();
                });
    }

    private void registerCurrentToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::register)
                .addOnFailureListener(e -> Log.w(TAG, "getToken", e));
    }

    private void register(@Nullable final String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        enqueue(api.registerFcmToken(authToken(), token, "android"), body -> body != null && body.statusRegister,
                (Resource<Boolean> r) -> {
                    if (r.isSuccess() && Boolean.TRUE.equals(r.data)) {
                        prefs.setString(KEY_REGISTERED_TOKEN, token);
                    } else {
                        Log.w(TAG, "registerFcmToken failed: " + r.message);
                    }
                });
    }
}
