package id.laukpauk.core;

import com.google.gson.JsonObject;

/** Port of lib/models/AuthModel.dart. Both methods are NETWORK calls (background thread). */
public final class AuthModel {
    private AuthModel() {}

    /** True when the stored auth token is non-empty and accepted by the server. */
    public static boolean isLogin() {
        boolean isLogin = false;
        String authToken = LocalStorage.getAuthToken();
        if (!authToken.isEmpty()) {
            Http.Response r = Http.getApi("/Auth/cekToken?authToken=" + authToken);
            if (r.statusCode == 200) {
                isLogin = Json.bool(r.jsonObject(), "cekToken");
            }
        }
        return isLogin;
    }

    /**
     * Dart returned bool?: null when not logged in or the request failed, otherwise the flag.
     */
    public static Boolean isOTPConfirmed() {
        Boolean result = null;
        if (isLogin()) {
            String authToken = LocalStorage.getAuthToken();
            Http.Response r = Http.getApi("/Auth/isOTPConfirmed?authToken=" + authToken);
            if (r.statusCode == 200) {
                JsonObject body = r.jsonObject();
                if (Json.has(body, "isOTPConfirmed")) {
                    result = Json.bool(body, "isOTPConfirmed");
                }
            }
        }
        return result;
    }
}
