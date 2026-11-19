package id.laukpauk.data.repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import id.laukpauk.data.model.search.SearchHistory;
import id.laukpauk.data.model.search.SearchSuggestion;
import androidx.annotation.Nullable;
import id.laukpauk.data.model.product.BuyerProduct;
import id.laukpauk.data.model.product.Paged;
import id.laukpauk.data.model.product.ProductResponses;
import id.laukpauk.data.remote.ProductApi;

/** Product search ({@code GET buyer/products?q=}) and the saved search history (legacy HistoriPencarianModel). */
public class SearchRepository extends BaseRepository {

    private static final java.lang.reflect.Type HISTORY_TYPE =
            com.squareup.moshi.Types.newParameterizedType(List.class, SearchHistory.class);

    private static final String HISTORY_KEY = "historiPencarian";

    private final ProductApi api;

    public SearchRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(ProductApi.class);
    }

    /** The first page of products matching {@code term} ({@code GET buyer/products?q=}). */
    public void search(String term, ResultCallback<Paged<SearchSuggestion>> callback) {
        enqueue(api.buyerProducts(ProductRepository.DEFAULT_LIMIT, term.trim()), SearchRepository::suggestions, callback);
    }

    /** The next page of a search: {@code next} is {@code data.next} of the previous page. */
    public void searchPage(String next, ResultCallback<Paged<SearchSuggestion>> callback) {
        enqueue(api.buyerProductsPage(next), SearchRepository::suggestions, callback);
    }

    private static Paged<SearchSuggestion> suggestions(@Nullable ProductResponses.BuyerProducts body) {
        Paged<BuyerProduct> page = ProductRepository.page(body);
        List<SearchSuggestion> list = new ArrayList<>();
        for (BuyerProduct p : page.items) {
            list.add(p.toSuggestion());
        }
        return new Paged<>(list, page.next);
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
