package id.laukpauk.data.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import id.laukpauk.data.model.search.SearchHistory;
import id.laukpauk.data.model.search.SearchSuggestion;
import id.laukpauk.data.remote.SearchApi;

/** Search suggestions (legacy SearchResult) and the saved search history (legacy HistoriPencarianModel). */
public class SearchRepository extends BaseRepository {

    private static final java.lang.reflect.Type HISTORY_TYPE =
            com.squareup.moshi.Types.newParameterizedType(List.class, SearchHistory.class);

    private static final String HISTORY_KEY = "historiPencarian";

    private final SearchApi api;

    public SearchRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(SearchApi.class);
    }

    /** Products matching {@code term}. */
    public void search(String term, ResultCallback<List<SearchSuggestion>> callback) {
        enqueue(api.search(term), body -> {
            List<SearchSuggestion> list = body == null ? null : body.searchResults;
            return list == null ? new ArrayList<SearchSuggestion>() : list;
        }, callback);
    }

    /** Saved searches, oldest first. */
    public List<SearchHistory> getSearchHistory() {
        String json = prefs.getString(HISTORY_KEY);
        if (json == null) {
            return new ArrayList<>();
        }
        List<SearchHistory> list = deps.apiClient.fromJson(json, HISTORY_TYPE);
        return list == null ? new ArrayList<SearchHistory>() : new ArrayList<>(list);
    }

    /** Appends a search with the time as "H:m:s d/M/yyyy" (legacy format). */
    public void addSearchHistory(String search) {
        Calendar now = Calendar.getInstance();
        String onTime = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND)
                + " " + now.get(Calendar.DAY_OF_MONTH) + "/" + (now.get(Calendar.MONTH) + 1) + "/"
                + now.get(Calendar.YEAR);
        List<SearchHistory> list = getSearchHistory();
        list.add(new SearchHistory(search, onTime));
        prefs.setString(HISTORY_KEY, deps.apiClient.toJson(list, HISTORY_TYPE));
    }
}
