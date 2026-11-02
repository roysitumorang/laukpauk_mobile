package id.laukpauk.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import id.laukpauk.data.Resource;
import id.laukpauk.data.local.SessionStore;
import id.laukpauk.data.local.Prefs;
import id.laukpauk.util.AppExecutors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Common plumbing for repositories: creating Retrofit APIs and turning a {@link Call} into
 * {@code LiveData<Resource<T>>} (or a one-shot callback).
 *
 * A repository owns one feature's data: it talks to its Retrofit API, reads/writes the session in
 * {@link SessionStore} / {@link Prefs}, and never touches the UI (no Context of an Activity, no dialogs,
 * no navigation).
 */
public abstract class BaseRepository {

    protected final RepositoryDeps deps;
    protected final SessionStore session;
    protected final Prefs prefs;
    protected final AppExecutors executors;

    protected BaseRepository(RepositoryDeps deps) {
        this.deps = deps;
        this.session = deps.session;
        this.prefs = deps.prefs;
        this.executors = deps.executors;
    }

    /** {@code api(NotifikasiApi.class)}: the Retrofit implementation of a feature API. */
    protected <S> S api(Class<S> service) {
        return deps.apiClient.retrofit().create(service);
    }

    /** The stored session token ("" when logged out), for endpoints with {@code @Query("authToken")}. */
    protected String authToken() {
        return session.getAuthToken();
    }

    /** Maps a successful response body to the value the UI needs. */
    public interface Mapper<T, R> {
        R map(@Nullable T body);
    }

    /** One-shot result callback, always invoked on the main thread. */
    public interface ResultCallback<R> {
        void onResult(@NonNull Resource<R> result);
    }

    /** Enqueues the call and exposes LOADING, then SUCCESS (mapped body) or ERROR. */
    protected <T, R> LiveData<Resource<R>> load(Call<T> call, Mapper<T, R> mapper) {
        MutableLiveData<Resource<R>> live = new MutableLiveData<>(Resource.loading());
        enqueue(call, mapper, live::setValue);
        return live;
    }

    protected <T> LiveData<Resource<T>> load(Call<T> call) {
        return load(call, body -> body);
    }

    /** Enqueues the call and reports the result once, on the main thread. */
    protected <T, R> void enqueue(Call<T> call, Mapper<T, R> mapper, ResultCallback<R> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> c, @NonNull Response<T> response) {
                Resource<R> result;
                if (response.isSuccessful()) {
                    R mapped;
                    try {
                        mapped = mapper.map(response.body());
                    } catch (RuntimeException e) {
                        callback.onResult(Resource.error(e.getMessage(), response.code()));
                        return;
                    }
                    result = Resource.success(mapped);
                } else {
                    result = Resource.error(response.message(), response.code());
                }
                callback.onResult(result);
            }

            @Override
            public void onFailure(@NonNull Call<T> c, @NonNull Throwable t) {
                callback.onResult(Resource.error(t.getMessage(), 0));
            }
        });
    }
}
