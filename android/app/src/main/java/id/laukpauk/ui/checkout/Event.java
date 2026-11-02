package id.laukpauk.ui.checkout;

/** A LiveData value that is delivered to the UI only once (navigation, dialogs). */
public final class Event<T> {
    private final T content;
    private boolean handled;

    public Event(T content) {
        this.content = content;
    }

    /** The content the first time, null afterwards. */
    public T consume() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
