package id.laukpauk.data.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import id.laukpauk.data.model.search.HistoriPencarian;
import id.laukpauk.data.model.search.SearchSuggestion;
import id.laukpauk.data.remote.SearchApi;

/** Search suggestions (legacy SearchResult) and the saved search history (legacy HistoriPencarianModel). */
public class SearchRepository extends BaseRepository {

    private static final java.lang.reflect.Type HISTORI_TYPE =
            com.squareup.moshi.Types.newParameterizedType(List.class, HistoriPencarian.class);

    private static final String KEY_HISTORI = "historiPencarian";

    private final SearchApi api;

    public SearchRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(SearchApi.class);
    }

    /** Products and sellers matching {@code term}. */
    public void search(String term, ResultCallback<List<SearchSuggestion>> callback) {
        enqueue(api.pencarian(term), body -> {
            List<SearchSuggestion> list = body == null ? null : body.hasilPencarian;
            return list == null ? new ArrayList<SearchSuggestion>() : list;
        }, callback);
    }

    /** Saved searches, oldest first. */
    public List<HistoriPencarian> getHistoriPencarian() {
        String json = prefs.getString(KEY_HISTORI);
        if (json == null) {
            return new ArrayList<>();
        }
        List<HistoriPencarian> list = deps.apiClient.fromJson(json, HISTORI_TYPE);
        return list == null ? new ArrayList<HistoriPencarian>() : new ArrayList<>(list);
    }

    /** Appends a search with the time as "H:m:s d/M/yyyy" (legacy format). */
    public void addHistoriPencarian(String search) {
        Calendar now = Calendar.getInstance();
        String onTime = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND)
                + " " + now.get(Calendar.DAY_OF_MONTH) + "/" + (now.get(Calendar.MONTH) + 1) + "/"
                + now.get(Calendar.YEAR);
        List<HistoriPencarian> list = getHistoriPencarian();
        list.add(new HistoriPencarian(search, onTime));
        prefs.setString(KEY_HISTORI, deps.apiClient.toJson(list, HISTORI_TYPE));
    }
}
