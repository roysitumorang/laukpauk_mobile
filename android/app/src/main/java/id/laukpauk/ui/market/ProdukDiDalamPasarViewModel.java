package id.laukpauk.ui.market;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.market.Pasar;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.data.repository.PasarRepository;
import id.laukpauk.data.repository.ProdukRepository;

/**
 * State of ProdukDiDalamPasar: the endless product list (pages of 10), the local search state, and the two
 * "Pasar Lain" lists.
 */
public class ProdukDiDalamPasarViewModel extends ViewModel {

    private static final int PAGE = 10;

    private final PasarRepository repository;
    private final String idPasar;
    private final LowestPriceStore prices;

    private final MutableLiveData<List<Produk>> products = new MutableLiveData<>(Collections.<Produk>emptyList());
    private final MutableLiveData<Boolean> initialLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> fetchingMore = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> searchTyped = new MutableLiveData<>(false);
    private final MutableLiveData<Resource<List<Pasar>>> around = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Pasar>>> over = new MutableLiveData<>();

    private List<Produk> immutable = new ArrayList<>();
    private int startFrom = 0;
    private int startFromSearch = PAGE;
    private boolean exhausted;
    private boolean exhaustedSearch;
    @Nullable private String pencarian;
    private int generation;
    @Nullable private String pasarLainKey;

    public ProdukDiDalamPasarViewModel(PasarRepository repository, ProdukRepository produkRepository, String idPasar) {
        this.repository = repository;
        this.idPasar = idPasar;
        this.prices = new LowestPriceStore(produkRepository);
        loadFirstPage();
    }

    public LiveData<List<Produk>> getProducts() {
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

    public LiveData<Resource<List<Pasar>>> getAround() {
        return around;
    }

    public LiveData<Resource<List<Pasar>>> getOver() {
        return over;
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
        repository.getProdukInThisPasar(idPasar, String.valueOf(startFrom), null, result -> {
            if (gen != generation) {
                return;
            }
            List<Produk> data = result.isSuccess() && result.data != null ? result.data : new ArrayList<Produk>();
            immutable = new ArrayList<>(data);
            if (pencarian == null) {
                products.setValue(data);
            }
            startFrom += PAGE;
            initialLoaded.setValue(true);
        });
    }

    /** Pull-to-refresh: reloads the first page and the other markets. */
    public void refresh() {
        pencarian = null;
        searchTyped.setValue(false);
        loadFirstPage();
        pasarLainKey = null;
    }

    /** Search submitted: server-side search for 1+ characters, otherwise the unfiltered list again. */
    public void search(String text) {
        if (text.length() >= 1) {
            final int gen = generation;
            repository.getProdukInThisPasar(idPasar, null, text, result -> {
                if (gen != generation) {
                    return;
                }
                pencarian = text;
                searchTyped.setValue(true);
                startFromSearch = PAGE;
                exhaustedSearch = false;
                products.setValue(result.isSuccess() && result.data != null ? result.data : new ArrayList<Produk>());
            });
        } else {
            pencarian = text;
            searchTyped.setValue(true);
            products.setValue(immutable);
        }
    }

    public void clearSearch() {
        pencarian = null;
        searchTyped.setValue(false);
        products.setValue(immutable);
    }

    /** The grid reached its end: loads the next 10 products (of the search when searching). */
    public void loadMore() {
        if (Boolean.TRUE.equals(fetchingMore.getValue()) || !Boolean.TRUE.equals(initialLoaded.getValue())) {
            return;
        }
        final boolean searching = pencarian != null && pencarian.length() >= 1;
        if (searching ? exhaustedSearch : exhausted) {
            return;
        }
        final int gen = generation;
        fetchingMore.setValue(true);
        String from = String.valueOf(searching ? startFromSearch : startFrom);
        repository.getProdukInThisPasar(idPasar, from, searching ? pencarian : null, result -> {
            if (gen != generation) {
                return;
            }
            List<Produk> next = result.isSuccess() && result.data != null ? result.data : new ArrayList<Produk>();
            if (searching) {
                startFromSearch += PAGE;
                exhaustedSearch = next.isEmpty();
            } else {
                startFrom += PAGE;
                exhausted = next.isEmpty();
                immutable = concat(immutable, next);
            }
            List<Produk> current = products.getValue();
            products.setValue(concat(current == null ? Collections.<Produk>emptyList() : current, next));
            fetchingMore.setValue(false);
        });
    }

    /** Loads the "Pasar Lain" lists for a delivery selection ({@code key} identifies it; repeated calls are ignored). */
    public void loadPasarLain(String key) {
        if (key.equals(pasarLainKey)) {
            return;
        }
        pasarLainKey = key;
        around.setValue(Resource.<List<Pasar>>loading());
        over.setValue(Resource.<List<Pasar>>loading());
        repository.getPasarAround(idPasar, around::setValue);
        repository.getPasarOver(over::setValue);
    }

    private static List<Produk> concat(List<Produk> a, List<Produk> b) {
        List<Produk> merged = new ArrayList<>(a);
        merged.addAll(b);
        return merged;
    }
}
