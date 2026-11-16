package id.laukpauk.ui.product;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.laukpauk.data.model.product.Product;
import id.laukpauk.data.repository.ProductRepository;

/** State of SemuaProduk: the endless product list (pages of 10) and the local search state. */
public class AllProductsViewModel extends ViewModel {

    private static final int PAGE = 10;

    private final ProductRepository repository;
    private final LowestPriceStore prices;

    private final MutableLiveData<List<Product>> products = new MutableLiveData<>(Collections.<Product>emptyList());
    private final MutableLiveData<Boolean> initialLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> fetchingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> searchTyped = new MutableLiveData<>(false);

    private List<Product> immutable = new ArrayList<>();
    private int startFrom = 0;
    private int startFromSearch = PAGE;
    private boolean exhausted;
    private boolean exhaustedSearch;
    @Nullable private String search;
    private int generation;

    public AllProductsViewModel(ProductRepository repository) {
        this.repository = repository;
        this.prices = new LowestPriceStore(repository);
        loadFirstPage();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<Boolean> getInitialLoaded() {
        return initialLoaded;
    }

    public LiveData<Boolean> getFetchingMore() {
        return fetchingMore;
    }

    public LiveData<Boolean> getSearchTyped() {
        return searchTyped;
    }

    public LowestPriceStore getPrices() {
        return prices;
    }

    private void loadFirstPage() {
        final int gen = ++generation;
        startFrom = 0;
        startFromSearch = PAGE;
        exhausted = false;
        exhaustedSearch = false;
        repository.getProduct(String.valueOf(startFrom), null, result -> {
            if (gen != generation) {
                return;
            }
            List<Product> data = result.isSuccess() && result.data != null ? result.data : new ArrayList<Product>();
            immutable = new ArrayList<>(data);
            if (search == null) {
                products.setValue(data);
            }
            startFrom += PAGE;
            initialLoaded.setValue(true);
        });
    }

    /** Pull-to-refresh: reloads the first page. */
    public void refresh() {
        search = null;
        searchTyped.setValue(false);
        loadFirstPage();
    }

    /** Search submitted: server-side search for 1+ characters, otherwise the unfiltered list again. */
    public void search(String text) {
        if (text.length() >= 1) {
            final int gen = generation;
            repository.getProduct(null, text, result -> {
                if (gen != generation) {
                    return;
                }
                search = text;
                searchTyped.setValue(true);
                startFromSearch = PAGE;
                exhaustedSearch = false;
                products.setValue(result.isSuccess() && result.data != null ? result.data : new ArrayList<Product>());
            });
        } else {
            search = text;
            searchTyped.setValue(true);
            products.setValue(immutable);
        }
    }

    public void clearSearch() {
        search = null;
        searchTyped.setValue(false);
        products.setValue(immutable);
    }

    /** The grid reached its end: loads the next 10 products (of the search when searching). */
    public void loadMore() {
        if (Boolean.TRUE.equals(fetchingMore.getValue()) || !Boolean.TRUE.equals(initialLoaded.getValue())) {
            return;
        }
        final boolean searching = search != null && search.length() >= 1;
        if (searching ? exhaustedSearch : exhausted) {
            return;
        }
        final int gen = generation;
        fetchingMore.setValue(true);
        String from = String.valueOf(searching ? startFromSearch : startFrom);
        repository.getProduct(from, searching ? search : null, result -> {
            if (gen != generation) {
                return;
            }
            List<Product> next = result.isSuccess() && result.data != null ? result.data : new ArrayList<Product>();
            if (searching) {
                startFromSearch += PAGE;
                exhaustedSearch = next.isEmpty();
            } else {
                startFrom += PAGE;
                exhausted = next.isEmpty();
                immutable = concat(immutable, next);
            }
            List<Product> current = products.getValue();
            products.setValue(concat(current == null ? Collections.<Product>emptyList() : current, next));
            fetchingMore.setValue(false);
        });
    }

    private static List<Product> concat(List<Product> a, List<Product> b) {
        List<Product> merged = new ArrayList<>(a);
        merged.addAll(b);
        return merged;
    }
}
