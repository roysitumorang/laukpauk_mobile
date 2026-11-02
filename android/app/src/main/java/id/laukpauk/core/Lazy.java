package id.laukpauk.core;

/**
 * A dependency that is resolved on first use instead of at construction time.
 *
 * Used only to break the two constructor cycles in the object graph (AppState -> HeaderModel and
 * HeaderModel -> TransaksiModel): {@link AppContainer} passes {@code () -> this.headerModel}, which is
 * read after the container has finished building, so {@link #get()} never returns null in practice.
 */
public interface Lazy<T> {
    T get();
}
