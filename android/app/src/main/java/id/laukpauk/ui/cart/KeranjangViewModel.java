package id.laukpauk.ui.cart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.CartGift;
import id.laukpauk.data.model.cart.CartProduct;
import id.laukpauk.data.model.cart.WalletConfiguration;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.CartRepository;

/** State of the Keranjang screen: cart lines, subtotal, wallet announcement and gifts. */
public class KeranjangViewModel extends ViewModel {

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
    }

    private final CartRepository repository;
    private final Slot<List<CartProduct>> products = new Slot<>();
    private final Slot<Integer> total = new Slot<>();
    private final Slot<WalletConfiguration> wallet = new Slot<>();
    private final Slot<List<CartGift>> gifts = new Slot<>();

    /** Failure text / button label (string resources) to show if the edit screen reports a failure. */
    public int pendingEditMessage;
    public int pendingEditButton;
    /** Set while the buyer went to Login from the Checkout button. */
    public boolean checkoutAfterLogin;

    public KeranjangViewModel(CartRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<CartProduct>>> getProducts() {
        return products.live;
    }

    public LiveData<Resource<Integer>> getTotal() {
        return total.live;
    }

    public LiveData<Resource<WalletConfiguration>> getWallet() {
        return wallet.live;
    }

    public LiveData<Resource<List<CartGift>>> getGifts() {
        return gifts.live;
    }

    /** Loads everything (first open, pull-to-refresh, after a change). */
    public void load() {
        repository.refreshCartCount();
        products.set(repository.cart());
        total.set(repository.totalKeranjang());
        wallet.set(repository.walletConfiguration());
        gifts.set(repository.gifts());
    }

    public void deleteItem(String idCart, BaseRepository.ResultCallback<Boolean> callback) {
        repository.deleteItem(idCart, callback);
    }

    public void clearCart(BaseRepository.ResultCallback<Boolean> callback) {
        repository.clearCart(callback);
    }
}
