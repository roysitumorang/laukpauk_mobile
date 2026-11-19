package id.laukpauk.ui.product;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.BuyerProduct;
import id.laukpauk.data.Paged;
import id.laukpauk.data.model.product.Product;
import id.laukpauk.data.repository.ProductRepository;

/**
 * State of the product grid: {@code GET buyer/products} pages of {@link ProductRepository#DEFAULT_LIMIT}, the next
 * page loaded from {@code data.next}, and the name search ({@code q}) with its own pages.
 */
public class AllProductsViewModel extends ViewModel {

    private final ProductRepository repository;
    private final LowestPriceStore prices;

    private final MutableLiveData<List<Product>> products = new MutableLiveData<>(Collections.<Product>emptyList());
    private final MutableLiveData<Boolean> initialLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> fetchingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> searchTyped = new MutableLiveData<>(false);

    /** The unfiltered list loaded so far and the URL of its next page (null = all loaded). */
    private List<Product> all = new ArrayList<>();
    @Nullable private String allNext;
    /** The search: its term (null = not searching) and the URL of its next page. */
    @Nullable private String search;
    @Nullable private String searchNext;
    /** Answers of superseded requests are dropped. */
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
        fetchingMore.setValue(false);
        repository.getBuyerProducts(null, ProductRepository.DEFAULT_LIMIT, result -> {
            if (gen != generation) {
                return;
            }
            Paged<BuyerProduct> page = pageOf(result);
            all = toProducts(page);
            allNext = page.next;
            if (search == null) {
                products.setValue(all);
            }
            initialLoaded.setValue(true);
        });
    }

    /** Pull-to-refresh: reloads the first page. */
    public void refresh() {
        search = null;
        searchNext = null;
        searchTyped.setValue(false);
        loadFirstPage();
    }

    /** Search submitted: {@code q} for 1+ non-blank characters, otherwise the unfiltered list again. */
    public void search(String text) {
        final String term = text == null ? "" : text.trim();
        if (term.isEmpty()) {
            clearSearch();
            return;
        }
        final int gen = ++generation;
        fetchingMore.setValue(false);
        repository.getBuyerProducts(term, ProductRepository.DEFAULT_LIMIT, result -> {
            if (gen != generation) {
                return;
            }
            Paged<BuyerProduct> page = pageOf(result);
            search = term;
            searchNext = page.next;
            searchTyped.setValue(true);
            products.setValue(toProducts(page));
        });
    }

    public void clearSearch() {
        ++generation;
        search = null;
        searchNext = null;
        searchTyped.setValue(false);
        fetchingMore.setValue(false);
        products.setValue(all);
    }

    /** The grid reached its end: loads the page at {@code data.next} (of the search when searching). */
    public void loadMore() {
        if (Boolean.TRUE.equals(fetchingMore.getValue()) || !Boolean.TRUE.equals(initialLoaded.getValue())) {
            return;
        }
        final boolean searching = search != null;
        final String next = searching ? searchNext : allNext;
        if (next == null) {
            return;
        }
        final int gen = generation;
        fetchingMore.setValue(true);
        repository.getBuyerProductsPage(next, result -> {
            if (gen != generation) {
                return;
            }
            fetchingMore.setValue(false);
            if (!result.isSuccess()) {
                return; // keep the URL: scrolling to the end again retries
            }
            Paged<BuyerProduct> page = pageOf(result);
            List<Product> more = toProducts(page);
            if (searching) {
                searchNext = page.next;
            } else {
                allNext = page.next;
                all = concat(all, more);
            }
            List<Product> current = products.getValue();
            products.setValue(concat(current == null ? Collections.<Product>emptyList() : current, more));
        });
    }

    private static Paged<BuyerProduct> pageOf(Resource<Paged<BuyerProduct>> result) {
        return result.isSuccess() && result.data != null ? result.data : Paged.<BuyerProduct>empty();
    }

    /** Grid tiles of a page; their lowest selling price goes straight into the price store. */
    private List<Product> toProducts(Paged<BuyerProduct> page) {
        List<Product> list = new ArrayList<>();
        for (BuyerProduct p : page.items) {
            prices.put(p.id, p.gridPrice());
            list.add(p.toProduct());
        }
        return list;
    }

    private static List<Product> concat(List<Product> a, List<Product> b) {
        List<Product> merged = new ArrayList<>(a);
        merged.addAll(b);
        return merged;
    }
}
