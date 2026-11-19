package id.laukpauk.data;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** One page of an infinite scroll list: its items and the URL of the next page (null = last page). */
public final class Paged<T> {
    public final List<T> items;
    @Nullable public final String next;

    public Paged(List<T> items, @Nullable String next) {
        this.items = items;
        this.next = next == null || next.trim().isEmpty() ? null : next;
    }

    public static <T> Paged<T> empty() {
        return new Paged<>(new ArrayList<T>(), null);
    }
}
