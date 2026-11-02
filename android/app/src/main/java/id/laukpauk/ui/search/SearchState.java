package id.laukpauk.ui.search;

import androidx.annotation.Nullable;

import java.util.List;

import id.laukpauk.data.model.search.SearchSuggestion;

/** What the suggestion area shows: the prompt, a spinner, the matches, or "not found". */
public final class SearchState {
    public enum Kind { PROMPT, LOADING, RESULTS, EMPTY }

    public final Kind kind;
    public final String term;
    @Nullable
    public final List<SearchSuggestion> list;

    public SearchState(Kind kind, String term, @Nullable List<SearchSuggestion> list) {
        this.kind = kind;
        this.term = term;
        this.list = list;
    }
}
