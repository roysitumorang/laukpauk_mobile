package id.laukpauk.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A value with its loading status, as exposed by repositories and ViewModels through LiveData
 * (the "Resource" pattern from Android's app-architecture guide).
 *
 * <pre>
 * viewModel.getItems().observe(getViewLifecycleOwner(), res -&gt; {
 *     binding.progress.setVisibility(res.isLoading() ? View.VISIBLE : View.GONE);
 *     if (res.isSuccess()) adapter.submitList(res.data);
 *     if (res.isError()) showError(res.message);
 * });
 * </pre>
 */
public final class Resource<T> {

    public enum Status { LOADING, SUCCESS, ERROR }

    @NonNull
    public final Status status;
    @Nullable
    public final T data;
    @Nullable
    public final String message;
    /** HTTP status code for errors that came from the server (0 = network failure / not an HTTP error). */
    public final int code;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message, int code) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.code = code;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null, 0);
    }

    public static <T> Resource<T> loading(@Nullable T previous) {
        return new Resource<>(Status.LOADING, previous, null, 0);
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null, 200);
    }

    public static <T> Resource<T> error(@Nullable String message, int code) {
        return new Resource<>(Status.ERROR, null, message, code);
    }

    public static <T> Resource<T> error(@Nullable String message, int code, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, message, code);
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
