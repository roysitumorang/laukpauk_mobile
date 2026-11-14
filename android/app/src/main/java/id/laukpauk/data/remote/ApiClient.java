package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import id.laukpauk.data.local.SessionStore;
import id.laukpauk.util.ServerPath;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Builds the single OkHttp client, Moshi instance and Retrofit for the backend at
 * {@link ServerPath#apiBaseURL}. Feature APIs are Retrofit interfaces in this package, created by the
 * repositories with {@code retrofit.create(XxxApi.class)}.
 *
 * Request and response bodies are JSON. Every request to the backend automatically carries these headers:
 * <ul>
 *   <li>{@code X-Version-Code}: the app's build number;</li>
 *   <li>{@code X-Request-Id}: a random UUID v4, new for each request;</li>
 *   <li>{@code Authorization: Bearer <JWT>}: the stored session token, when signed in.</li>
 * </ul>
 * Requests to other hosts (the Google Maps web services) get none of them.
 */
public final class ApiClient {

    public static final String HEADER_VERSION_CODE = "X-Version-Code";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String JSON = "application/json";

    private final Moshi moshi;
    private final OkHttpClient okHttp;
    private final Retrofit retrofit;

    public ApiClient(SessionStore session) {
        moshi = createMoshi();
        final String versionCode = session.getBuildNumber();
        final String base = ServerPath.apiBaseURL.endsWith("/") ? ServerPath.apiBaseURL : ServerPath.apiBaseURL + "/";
        final HttpUrl baseUrl = HttpUrl.parse(base);
        final String apiHost = baseUrl == null ? null : baseUrl.host();
        okHttp = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    if (apiHost == null || !apiHost.equals(request.url().host())) {
                        return chain.proceed(request);
                    }
                    Request.Builder builder = request.newBuilder()
                            .header(HEADER_ACCEPT, JSON)
                            .header(HEADER_VERSION_CODE, versionCode)
                            .header(HEADER_REQUEST_ID, UUID.randomUUID().toString());
                    String token = session.getAuthToken();
                    if (token != null && !token.isEmpty() && request.header(HEADER_AUTHORIZATION) == null) {
                        builder.header(HEADER_AUTHORIZATION, "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(base)
                .client(okHttp)
                .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
                .build();
    }

    /** The app's Moshi configuration: untyped trees with Long whole numbers, lenient scalars and empty objects. */
    public static Moshi createMoshi() {
        return new Moshi.Builder()
                .add(Object.class, new id.laukpauk.util.JsonTreeAdapter())
                .add(new LenientJsonAdapterFactory())
                .build();
    }

    public Retrofit retrofit() {
        return retrofit;
    }

    /** The app's Moshi (with {@link LenientJsonAdapterFactory}), also for JSON passed between screens. */
    public Moshi moshi() {
        return moshi;
    }

    /** Decodes JSON text into {@code type} (e.g. {@code Types.newParameterizedType(List.class, X.class)}); null when it can't. */
    @Nullable
    public <T> T fromJson(@Nullable String json, Type type) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            JsonAdapter<T> adapter = moshi.adapter(type);
            return adapter.lenient().fromJson(json);
        } catch (Exception e) {
            return null;
        }
    }

    /** Converts an untyped tree ({@code Map}/{@code List}, see util.Json) into {@code type}; null when it can't. */
    @Nullable
    public <T> T fromTree(@Nullable Object tree, Type type) {
        if (tree == null) {
            return null;
        }
        try {
            JsonAdapter<T> adapter = moshi.adapter(type);
            return adapter.fromJsonValue(tree);
        } catch (Exception e) {
            return null;
        }
    }

    /** Encodes {@code value} as JSON text using the adapter for {@code type}. */
    public <T> String toJson(@Nullable T value, Type type) {
        JsonAdapter<T> adapter = moshi.adapter(type);
        return adapter.toJson(value);
    }

    public OkHttpClient okHttp() {
        return okHttp;
    }
}
