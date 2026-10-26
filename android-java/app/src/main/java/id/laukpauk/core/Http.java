package id.laukpauk.core;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Blocking HTTP client replacing Dart's `package:http` (`get`, `post`).
 *
 * ALL methods are synchronous and must be called from a background thread
 * (see {@link Async}). They never throw: network failures produce a
 * {@link Response} with statusCode 0, so the usual Dart pattern
 *
 *     if (httpResponse.statusCode == 200) { ... }
 *
 * ports directly to
 *
 *     Http.Response r = Http.getApi("/Auth/cekToken?authToken=" + token);
 *     if (r.statusCode == 200) { ... }
 */
public final class Http {
    private Http() {}

    private static final String TAG = "Http";

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /** Mirrors package:http's Response: statusCode + body. */
    public static final class Response {
        public final int statusCode;
        public final String body;

        public Response(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body == null ? "" : body;
        }

        public boolean ok() {
            return statusCode == 200;
        }

        /** jsonDecode(body); null when the body is not valid JSON. */
        public JsonElement json() {
            return Json.parse(body);
        }

        /** jsonDecode(body) as a Map; an EMPTY object when it is not an object. */
        public JsonObject jsonObject() {
            return Json.obj(json());
        }

        /** jsonDecode(body) as a List; an EMPTY array when it is not an array. */
        public JsonArray jsonArray() {
            return Json.arr(json());
        }
    }

    // ------------------------------------------------------------------ GET

    /** GET an absolute URL. */
    public static Response get(String url) {
        try {
            Request request = new Request.Builder().url(url).get().build();
            return execute(request);
        } catch (RuntimeException e) { // e.g. IllegalArgumentException for a malformed URL
            Log.w(TAG, "GET failed: " + url, e);
            return new Response(0, "");
        }
    }

    /** GET ServerPath.apiBaseURL + pathAndQuery  (pathAndQuery starts with "/"). */
    public static Response getApi(String pathAndQuery) {
        return get(ServerPath.apiBaseURL + pathAndQuery);
    }

    // ------------------------------------------------------------------ POST (form-urlencoded)

    /** POST an application/x-www-form-urlencoded body to an absolute URL. */
    public static Response post(String url, Map<String, String> form) {
        try {
            FormBody.Builder body = new FormBody.Builder();
            if (form != null) {
                for (Map.Entry<String, String> e : form.entrySet()) {
                    body.add(e.getKey(), e.getValue() == null ? "" : e.getValue());
                }
            }
            Request request = new Request.Builder().url(url).post(body.build()).build();
            return execute(request);
        } catch (RuntimeException e) {
            Log.w(TAG, "POST failed: " + url, e);
            return new Response(0, "");
        }
    }

    /** POST form to ServerPath.apiBaseURL + pathAndQuery. */
    public static Response postApi(String pathAndQuery, Map<String, String> form) {
        return post(ServerPath.apiBaseURL + pathAndQuery, form);
    }

    /** POST a raw body (e.g. JSON text) with the given content type. */
    public static Response postRaw(String url, String body, String contentType) {
        try {
            RequestBody rb = RequestBody.create(body == null ? "" : body, MediaType.parse(contentType));
            Request request = new Request.Builder().url(url).post(rb).build();
            return execute(request);
        } catch (RuntimeException e) {
            Log.w(TAG, "POST failed: " + url, e);
            return new Response(0, "");
        }
    }

    // ------------------------------------------------------------------ POST (multipart / uploads)

    /**
     * POST multipart/form-data. `fields` are plain text parts, `files` maps the part name to a
     * local image file (sent as image/jpeg unless the name ends with .png).
     */
    public static Response postMultipart(String url, Map<String, String> fields,
                                         Map<String, File> files) {
        try {
            MultipartBody.Builder body = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if (fields != null) {
                for (Map.Entry<String, String> e : fields.entrySet()) {
                    body.addFormDataPart(e.getKey(), e.getValue() == null ? "" : e.getValue());
                }
            }
            if (files != null) {
                for (Map.Entry<String, File> e : files.entrySet()) {
                    File f = e.getValue();
                    if (f == null) {
                        continue;
                    }
                    String type = f.getName().toLowerCase().endsWith(".png")
                            ? "image/png" : "image/jpeg";
                    body.addFormDataPart(e.getKey(), f.getName(),
                            RequestBody.create(f, MediaType.parse(type)));
                }
            }
            Request request = new Request.Builder().url(url).post(body.build()).build();
            return execute(request);
        } catch (RuntimeException e) {
            Log.w(TAG, "multipart POST failed: " + url, e);
            return new Response(0, "");
        }
    }

    public static Response postMultipartApi(String pathAndQuery, Map<String, String> fields,
                                            Map<String, File> files) {
        return postMultipart(ServerPath.apiBaseURL + pathAndQuery, fields, files);
    }

    // ------------------------------------------------------------------ helpers

    /** URL-encodes a single query/form value (UTF-8). Use it for every user-typed value. */
    public static String enc(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException e) {
            return value; // UTF-8 is always supported
        }
    }

    private static Response execute(Request request) {
        try (okhttp3.Response r = CLIENT.newCall(request).execute()) {
            String text = r.body() != null ? r.body().string() : "";
            return new Response(r.code(), text);
        } catch (IOException e) {
            Log.w(TAG, "Request failed: " + request.url(), e);
            return new Response(0, "");
        }
    }
}
