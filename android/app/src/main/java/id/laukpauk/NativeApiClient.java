package id.laukpauk;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Small dependency-free JSON HTTP client for native feature migrations.
 *
 * Callers must provide the trusted API base URL from build configuration.
 * Network calls run off the main thread and callbacks return on the UI thread.
 */
public final class NativeApiClient {
    public interface Callback {
        void onSuccess(int statusCode, JSONObject body);
        void onError(Exception error);
    }

    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private NativeApiClient() { }

    public static void get(String baseUrl, String path, Map<String, String> query,
                           Callback callback) {
        request("GET", baseUrl, path, query, null, null, callback);
    }

    public static void postJson(String baseUrl, String path, JSONObject body,
                                Callback callback) {
        request("POST", baseUrl, path, null, body, null, callback);
    }

    public static void postForm(String baseUrl, String path, Map<String, String> query,
                                Map<String, String> formBody, Callback callback) {
        request("POST", baseUrl, path, query, null, formBody, callback);
    }

    private static void request(String method, String baseUrl, String path,
                                Map<String, String> query, JSONObject body,
                                Map<String, String> formBody, Callback callback) {
        IO.execute(() -> {
            HttpURLConnection connection = null;
            try {
                StringBuilder address = new StringBuilder(baseUrl);
                if (path != null) address.append(path.startsWith("/") ? path : "/" + path);
                if (query != null && !query.isEmpty()) {
                    address.append(address.indexOf("?") >= 0 ? "&" : "?");
                    Iterator<Map.Entry<String, String>> iterator = query.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> entry = iterator.next();
                        address.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                               .append("=")
                               .append(URLEncoder.encode(entry.getValue() == null ? "" : entry.getValue(), "UTF-8"));
                        if (iterator.hasNext()) address.append("&");
                    }
                }

                connection = (HttpURLConnection) new URL(address.toString()).openConnection();
                connection.setRequestMethod(method);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);
                connection.setRequestProperty("Accept", "application/json");

                if (body != null || formBody != null) {
                    connection.setDoOutput(true);
                    byte[] bytes;
                    if (formBody != null) {
                        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                        StringBuilder encoded = new StringBuilder();
                        Iterator<Map.Entry<String, String>> formIterator = formBody.entrySet().iterator();
                        while (formIterator.hasNext()) {
                            Map.Entry<String, String> entry = formIterator.next();
                            if (encoded.length() > 0) encoded.append("&");
                            encoded.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                                   .append("=")
                                   .append(URLEncoder.encode(entry.getValue() == null ? "" : entry.getValue(), "UTF-8"));
                        }
                        bytes = encoded.toString().getBytes(StandardCharsets.UTF_8);
                    } else {
                        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                        bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                    }
                    try (OutputStream out = connection.getOutputStream()) {
                        out.write(bytes);
                    }
                }

                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 400
                        ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder text = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) text.append(line);
                    }
                }
                JSONObject json = text.length() == 0 ? new JSONObject() : new JSONObject(text.toString());
                final int responseCode = status;
                MAIN.post(() -> callback.onSuccess(responseCode, json));
            } catch (Exception error) {
                MAIN.post(() -> callback.onError(error));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }
}
