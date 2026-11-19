package id.laukpauk.ui.search;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.model.search.SearchHistory;
import androidx.annotation.Nullable;
import id.laukpauk.data.model.product.Paged;
import id.laukpauk.data.model.search.SearchSuggestion;
import id.laukpauk.data.repository.SearchRepository;

/** Product search ({@code GET buyer/products?q=}, debounced 500 ms, more pages from {@code data.next}) and the history. */
public class SearchViewModel extends ViewModel {

    private static final long DEBOUNCE_MS = 500L;

    private final SearchRepository repository;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<SearchState> state =
            new MutableLiveData<>(new SearchState(SearchState.Kind.PROMPT, "", null));
    private final MutableLiveData<List<SearchHistory>> history = new MutableLiveData<>();
    private Runnable pending;
    private int generation;
    private String term = "";
    /** {@code data.next} of the last loaded result page (null = no more). */
    @Nullable private String next;
    private boolean loadingMore;

    public SearchViewModel(SearchRepository repository) {
        this.repository = repository;
        loadHistory();
    }

    public LiveData<SearchState> getState() {
        return state;
    }

    public LiveData<List<SearchHistory>> getHistory() {
        return history;
    }

    public String getTerm() {
        return term;
    }

    /** The field's text changed. A blank term shows the prompt; otherwise suggestions load after the debounce. */
    public void setTerm(String value) {
        String t = value == null ? "" : value;
        if (t.equals(term)) {
            return;
        }
        term = t;
        final int mine = ++generation;
        if (pending != null) {
            handler.removeCallbacks(pending);
        }
        if (t.trim().isEmpty()) {
            state.setValue(new SearchState(SearchState.Kind.PROMPT, t, null));
            return;
        }
        state.setValue(new SearchState(SearchState.Kind.LOADING, t, null));
        next = null;
        loadingMore = false;
        pending = () -> repository.search(t, result -> {
            if (mine != generation) {
                return;
            }
            Paged<SearchSuggestion> page = result.isSuccess() && result.data != null ? result.data
                    : Paged.<SearchSuggestion>empty();
            next = page.next;
            state.setValue(new SearchState(page.items.isEmpty() ? SearchState.Kind.EMPTY : SearchState.Kind.RESULTS,
                    t, page.items));
        });
        handler.postDelayed(pending, DEBOUNCE_MS);
    }

    /** The result list reached its end: appends the page at {@code data.next}. */
    public void loadMore() {
        SearchState current = state.getValue();
        if (loadingMore || next == null || current == null || current.kind != SearchState.Kind.RESULTS) {
            return;
        }
        final int mine = generation;
        final String url = next;
        loadingMore = true;
        repository.searchPage(url, result -> {
            if (mine != generation) {
                return;
            }
            loadingMore = false;
            if (!result.isSuccess() || result.data == null) {
                return; // keep the URL: scrolling to the end again retries
            }
            next = result.data.next;
            SearchState now = state.getValue();
            List<SearchSuggestion> merged = new ArrayList<>(now == null || now.list == null
                    ? new ArrayList<SearchSuggestion>() : now.list);
            merged.addAll(result.data.items);
            state.setValue(new SearchState(SearchState.Kind.RESULTS, term, merged));
        });
    }

    public void addHistory(String search) {
        if (search.trim().isEmpty()) {
            return;
        }
        repository.addSearchHistory(search);
        loadHistory();
    }

    private void loadHistory() {
        history.setValue(repository.getSearchHistory());
    }

    @Override
    protected void onCleared() {
        if (pending != null) {
            handler.removeCallbacks(pending);
        }
    }
}
