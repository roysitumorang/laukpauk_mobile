package id.laukpauk.data.model.search;

import java.util.List;

/** Response envelopes of the search endpoints. */
public final class SearchResponses {
    private SearchResponses() {}

    public static class Suggestions {
        public List<SearchSuggestion> hasilPencarian;
    }
}
