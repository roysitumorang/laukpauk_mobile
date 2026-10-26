package id.laukpauk.data;

import android.content.Context;
import id.laukpauk.ApiConfiguration;
import id.laukpauk.BuildConfig;
import id.laukpauk.NativeApiClient;
import id.laukpauk.NativePreferences;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared native equivalent of lib/libraries/HTTPRequest.dart.
 * Keeps versionCode and optional authToken conventions consistent across API calls. */
public final class NativeApiRepository {
    private NativeApiRepository() { }

    public static Map<String, String> query(Context context, boolean includeAuth,
                                             Map<String, String> parameters) {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("versionCode", String.valueOf(BuildConfig.VERSION_CODE));
        if (includeAuth) {
            String token = NativePreferences.getAuthToken(context);
            if (token != null && !token.isEmpty()) query.put("authToken", token);
        }
        if (parameters != null) query.putAll(parameters);
        return query;
    }

    public static void get(Context context, String path, boolean includeAuth,
                           Map<String, String> parameters, NativeApiClient.Callback callback) {
        NativeApiClient.get(ApiConfiguration.baseUrl(), path,
                query(context, includeAuth, parameters), callback);
    }

    public static void post(Context context, String path, boolean includeAuth,
                            JSONObject body, NativeApiClient.Callback callback) {
        JSONObject payload;
        try {
            payload = body == null ? new JSONObject() : new JSONObject(body.toString());
            payload.put("versionCode", String.valueOf(BuildConfig.VERSION_CODE));
            if (includeAuth) {
                String token = NativePreferences.getAuthToken(context);
                if (token != null && !token.isEmpty() && !payload.has("authToken")) {
                    payload.put("authToken", token);
                }
            }
        } catch (Exception e) {
            callback.onError(e);
            return;
        }
        NativeApiClient.postJson(ApiConfiguration.baseUrl(), path, payload, callback);
    }

    /** Form-encoded equivalent of package:http post(url, body: map). */
    public static void postForm(Context context, String path, boolean includeAuth,
                                Map<String, String> body, Map<String, String> queryParameters,
                                NativeApiClient.Callback callback) {
        Map<String, String> payload = new LinkedHashMap<>();
        if (body != null) payload.putAll(body);
        payload.put("versionCode", String.valueOf(BuildConfig.VERSION_CODE));
        NativeApiClient.postForm(ApiConfiguration.baseUrl(), path,
                query(context, includeAuth, queryParameters), payload, callback);
    }

    public static Map<String, String> params(String... pairs) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) result.put(pairs[i], pairs[i + 1]);
        return result;
    }
}
