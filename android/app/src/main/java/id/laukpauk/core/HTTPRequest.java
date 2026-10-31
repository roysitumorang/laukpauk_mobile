package id.laukpauk.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Port of lib/libraries/HTTPRequest.dart. BLOCKING (isLogin makes a request when authToken is true):
 * call from a background thread.
 */
public final class HTTPRequest {
    private HTTPRequest() {}

    private static final String versionCodeKey = "versionCode";

    /** Dart: HTTPRequest.postBody(body: {...}) - the form map with versionCode added first. */
    public static Map<String, String> postBody(Map<String, String> body) {
        Map<String, String> returned = new LinkedHashMap<>();
        returned.put(versionCodeKey, LocalStorage.getBuildNumber());
        if (body != null) {
            returned.putAll(body);
        }
        return returned;
    }

    public static Map<String, String> postBody() {
        return postBody(null);
    }

    /** Dart: getQueryString() -> "versionCode=50600". */
    public static String getQueryString() {
        return getQueryString(null, false);
    }

    /** Dart: getQueryString(params: 'a=1&b=2'). Encode user-typed values with Http.enc(...). */
    public static String getQueryString(String params) {
        return getQueryString(params, false);
    }

    /**
     * Dart: getQueryString(params: ..., authToken: true). With authToken, the stored token is only
     * sent when the server confirms it is still valid (empty token otherwise).
     */
    public static String getQueryString(String params, boolean authToken) {
        String returned = versionCodeKey + "=" + LocalStorage.getBuildNumber();

        if (authToken) {
            boolean loggedIn = AuthModel.isLogin();
            String token = loggedIn ? LocalStorage.getAuthToken() : "";
            returned = returned + "&authToken=" + token;
        }

        if (params != null) {
            returned = returned + "&" + params;
        }
        return returned;
    }
}
