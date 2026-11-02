package id.laukpauk.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Port of lib/libraries/HTTPRequest.dart. BLOCKING (isLogin makes a request when authToken is true):
 * call from a background thread.
 */
public final class HTTPRequest {
    private final AuthModel authModel;
    private final LocalStorage localStorage;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public HTTPRequest(AuthModel authModel, LocalStorage localStorage) {
        this.authModel = authModel;
        this.localStorage = localStorage;
    }

    private static final String versionCodeKey = "versionCode";

    /** Dart: HTTPRequest.postBody(body: {...}) - the form map with versionCode added first. */
    public Map<String, String> postBody(Map<String, String> body) {
        Map<String, String> returned = new LinkedHashMap<>();
        returned.put(versionCodeKey, localStorage.getBuildNumber());
        if (body != null) {
            returned.putAll(body);
        }
        return returned;
    }

    public Map<String, String> postBody() {
        return postBody(null);
    }

    /** Dart: getQueryString() -> "versionCode=50600". */
    public String getQueryString() {
        return getQueryString(null, false);
    }

    /** Dart: getQueryString(params: 'a=1&b=2'). Encode user-typed values with Http.enc(...). */
    public String getQueryString(String params) {
        return getQueryString(params, false);
    }

    /**
     * Dart: getQueryString(params: ..., authToken: true). With authToken, the stored token is only
     * sent when the server confirms it is still valid (empty token otherwise).
     */
    public String getQueryString(String params, boolean authToken) {
        String returned = versionCodeKey + "=" + localStorage.getBuildNumber();

        if (authToken) {
            boolean loggedIn = authModel.isLogin();
            String token = loggedIn ? localStorage.getAuthToken() : "";
            returned = returned + "&authToken=" + token;
        }

        if (params != null) {
            returned = returned + "&" + params;
        }
        return returned;
    }
}
