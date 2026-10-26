package id.laukpauk;

import android.content.Context;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Authentication endpoint adapter ported from lib/models/AuthModel.dart. */
public final class NativeAuthService {
    public interface BooleanCallback {
        void onResult(boolean value);
        void onError(Exception error);
    }

    public interface NullableBooleanCallback {
        void onResult(Boolean value);
        void onError(Exception error);
    }

    private NativeAuthService() { }

    public static void isLoggedIn(Context context, BooleanCallback callback) {
        String token = NativePreferences.getAuthToken(context);
        if (token.isEmpty()) {
            callback.onResult(false);
            return;
        }
        Map<String, String> query = new HashMap<>();
        query.put("authToken", token);
        NativeApiClient.get(ApiConfiguration.baseUrl(), "/Auth/cekToken", query,
                new NativeApiClient.Callback() {
                    @Override public void onSuccess(int status, org.json.JSONObject body) {
                        callback.onResult(status == 200 && body.optBoolean("cekToken", false));
                    }
                    @Override public void onError(Exception error) { callback.onError(error); }
                });
    }

    /** Matches the old isOTPConfirmed behavior: unknown when unauthenticated or endpoint fails. */
    public static void isOtpConfirmed(Context context, NullableBooleanCallback callback) {
        isLoggedIn(context, new BooleanCallback() {
            @Override public void onResult(boolean loggedIn) {
                if (!loggedIn) {
                    callback.onResult(null);
                    return;
                }
                Map<String, String> query = Collections.singletonMap(
                        "authToken", NativePreferences.getAuthToken(context));
                NativeApiClient.get(ApiConfiguration.baseUrl(), "/Auth/isOTPConfirmed", query,
                        new NativeApiClient.Callback() {
                            @Override public void onSuccess(int status, org.json.JSONObject body) {
                                callback.onResult(status == 200
                                        ? body.optBoolean("isOTPConfirmed", false) : null);
                            }
                            @Override public void onError(Exception error) { callback.onError(error); }
                        });
            }
            @Override public void onError(Exception error) { callback.onError(error); }
        });
    }
}
