package id.laukpauk.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import id.laukpauk.data.local.SessionStore;
import id.laukpauk.util.ServerPath;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds the single OkHttp client, Gson instance and Retrofit for the backend at
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

    private final Gson gson;
    private final OkHttpClient okHttp;
    private final Retrofit retrofit;

    public ApiClient(SessionStore session) {
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new LenientTypeAdapterFactory())
                .create();
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
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public Retrofit retrofit() {
        return retrofit;
    }

    public Gson gson() {
        return gson;
    }

    public OkHttpClient okHttp() {
        return okHttp;
    }
}
