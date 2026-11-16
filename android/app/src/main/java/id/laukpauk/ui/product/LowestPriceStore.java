package id.laukpauk.ui.product;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import id.laukpauk.data.model.product.LowestPrice;
import id.laukpauk.data.repository.ProductRepository;

/**
 * Lowest price per product id for the product tiles (each legacy tile loaded its own price). Owned by a
 * ViewModel; {@link LowestPrice#NONE} marks "no price list" (also after a failed request).
 */
public class LowestPriceStore {

    private final ProductRepository repository;
    private final Set<String> requested = new HashSet<>();
    private final Map<String, LowestPrice> prices = new HashMap<>();
    private final MutableLiveData<Map<String, LowestPrice>> live = new MutableLiveData<>(new HashMap<>());

    public LowestPriceStore(ProductRepository repository) {
        this.repository = repository;
    }

    public LiveData<Map<String, LowestPrice>> getPrices() {
        return live;
    }

    /** Starts loading the price of a product once. */
    public void request(String productId) {
        if (productId == null || !requested.add(productId)) {
            return;
        }
        repository.getLowestPrice(productId, result -> {
            prices.put(productId, result.isSuccess() && result.data != null ? result.data : LowestPrice.NONE);
            live.setValue(new HashMap<>(prices));
        });
    }
}
