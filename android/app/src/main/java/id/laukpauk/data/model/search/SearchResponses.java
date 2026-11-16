package id.laukpauk.data.model.search;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the search endpoints. */
public final class SearchResponses {
    private SearchResponses() {}

    public static class Suggestions {
        @Json(name = "hasilPencarian")
        public List<SearchSuggestion> searchResults;
    }
}
