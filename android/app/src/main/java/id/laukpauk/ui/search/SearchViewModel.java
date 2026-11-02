package id.laukpauk.ui.search;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.model.search.HistoriPencarian;
import id.laukpauk.data.repository.SearchRepository;

/** Search suggestions for the typed term (debounced 500 ms like the legacy SearchResult) and the history. */
public class SearchViewModel extends ViewModel {

    private static final long DEBOUNCE_MS = 500L;

    private final SearchRepository repository;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<SearchState> state =
            new MutableLiveData<>(new SearchState(SearchState.Kind.PROMPT, "", null));
    private final MutableLiveData<List<HistoriPencarian>> histori = new MutableLiveData<>();
    private Runnable pending;
    private int generation;
    private String term = "";

    public SearchViewModel(SearchRepository repository) {
        this.repository = repository;
        loadHistori();
    }

    public LiveData<SearchState> getState() {
        return state;
    }

    public LiveData<List<HistoriPencarian>> getHistori() {
        return histori;
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
        pending = () -> repository.search(t, result -> {
            if (mine != generation) {
                return;
            }
            List<id.laukpauk.data.model.search.SearchSuggestion> list =
                    result.isSuccess() && result.data != null ? result.data : new ArrayList<>();
            state.setValue(new SearchState(list.isEmpty() ? SearchState.Kind.EMPTY : SearchState.Kind.RESULTS, t, list));
        });
        handler.postDelayed(pending, DEBOUNCE_MS);
    }

    public void addHistori(String search) {
        if (search.trim().isEmpty()) {
            return;
        }
        repository.addHistoriPencarian(search);
        loadHistori();
    }

    private void loadHistori() {
        histori.setValue(repository.getHistoriPencarian());
    }

    @Override
    protected void onCleared() {
        if (pending != null) {
            handler.removeCallbacks(pending);
        }
    }
}
