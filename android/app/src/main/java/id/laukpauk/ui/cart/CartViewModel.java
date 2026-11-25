package id.laukpauk.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.CartGift;
import id.laukpauk.data.model.cart.BuyerCart;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.CartRepository;

/** State of the Keranjang screen: the buyer's cart ({@code GET buyer/cart}) and gifts. */
public class CartViewModel extends ViewModel {

    /** A LiveData that can be re-pointed at a freshly loaded source (refresh). */
    private static final class Slot<T> {
        final MediatorLiveData<Resource<T>> live = new MediatorLiveData<>();
        private LiveData<Resource<T>> source;

        void set(LiveData<Resource<T>> next) {
            if (source != null) {
                live.removeSource(source);
            }
            source = next;
            live.addSource(next, live::setValue);
        }

        void post(Resource<T> value) {
            if (source != null) {
                live.removeSource(source);
                source = null;
            }
            live.setValue(value);
        }
    }

    private final CartRepository repository;
    private final Slot<BuyerCart> cart = new Slot<>();
    private final Slot<List<CartGift>> gifts = new Slot<>();

    /** Set while the buyer went to Login from the Checkout button. */
    public boolean checkoutAfterLogin;

    public CartViewModel(CartRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<BuyerCart>> getCart() {
        return cart.live;
    }

    public LiveData<Resource<List<CartGift>>> getGifts() {
        return gifts.live;
    }

    /** Loads everything (first open, pull-to-refresh, back from Login). */
    public void load() {
        cart.set(repository.buyerCart());
        gifts.set(repository.gifts());
    }

    /** Sets an item's quantity (0 removes it); the saved cart replaces the shown one. */
    public void setQuantity(String variantId, long quantity, BaseRepository.ResultCallback<BuyerCart> callback) {
        repository.setVariantQuantity(variantId, quantity, saved(callback));
    }

    /** Sets an item's note; an empty note removes it. */
    public void setNote(String variantId, String note, BaseRepository.ResultCallback<BuyerCart> callback) {
        repository.setVariantNote(variantId, note, saved(callback));
    }

    public void clearCart(BaseRepository.ResultCallback<BuyerCart> callback) {
        repository.clearBuyerCart(saved(callback));
    }

    private BaseRepository.ResultCallback<BuyerCart> saved(BaseRepository.ResultCallback<BuyerCart> callback) {
        return result -> {
            if (result.isSuccess()) {
                cart.post(Resource.success(result.data == null ? new BuyerCart() : result.data));
            }
            callback.onResult(result);
        };
    }
}
