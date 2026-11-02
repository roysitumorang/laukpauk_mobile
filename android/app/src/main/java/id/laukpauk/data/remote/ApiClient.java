package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import id.laukpauk.data.local.SessionStore;
import id.laukpauk.util.ServerPath;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Builds the single OkHttp client, Moshi instance and Retrofit for the backend at
 * {@link ServerPath#apiBaseURL}. Feature APIs are Retrofit interfaces in this package, created by the
 * repositories with {@code retrofit.create(XxxApi.class)}.
 *
 * Every request automatically carries {@code versionCode=<build number>} the way the Dart app's
 * HTTPRequest helper added it: as a query parameter on every request, and additionally as the FIRST form
 * field of url-encoded POST bodies (the server may read it from either place).
 * Endpoints that need the session declare {@code @Query("authToken") String authToken} and repositories
 * pass {@code session.getAuthToken()}.
 */
public final class ApiClient {

    private final Moshi moshi;
    private final OkHttpClient okHttp;
    private final Retrofit retrofit;

    public ApiClient(SessionStore session) {
        moshi = createMoshi();
        final String versionCode = session.getBuildNumber();
        okHttp = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    HttpUrl url = request.url().newBuilder()
                            .setQueryParameter("versionCode", versionCode)
                            .build();
                    Request.Builder builder = request.newBuilder().url(url);
                    RequestBody body = request.body();
                    if (body instanceof FormBody) {
                        FormBody form = (FormBody) body;
                        FormBody.Builder withVersion = new FormBody.Builder().add("versionCode", versionCode);
                        for (int i = 0; i < form.size(); i++) {
                            if (!form.name(i).equals("versionCode")) {
                                withVersion.addEncoded(form.encodedName(i), form.encodedValue(i));
                            }
                        }
                        builder.method(request.method(), withVersion.build());
                    }
                    return chain.proceed(builder.build());
                })
                .build();
        String base = ServerPath.apiBaseURL.endsWith("/") ? ServerPath.apiBaseURL : ServerPath.apiBaseURL + "/";
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
