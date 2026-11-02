package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.AddToCartResult;

/**
 * The shopping cart (legacy CheckoutModel cart parts, ProdukModel.addToCart, HeaderModel.getJumlahCart,
 * TransaksiModel.removeTransactionSession, AppState.jumlahCart).
 *
 * OWNER: the cart slice. The public methods below are a CONTRACT used by other slices (header badge,
 * product buy sheet, checkout, payment results): keep their signatures and implement the bodies; add
 * whatever else the cart screens need.
 */
public class CartRepository extends BaseRepository {

    private final DeliveryRepository delivery;
    private final MutableLiveData<Integer> cartCount = new MutableLiveData<>(0);

    public CartRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.delivery = delivery;
    }

    /** Number of items in the cart (header badge). */
    public LiveData<Integer> cartCount() {
        return cartCount;
    }

    /** Reloads {@link #cartCount()} from the server (legacy HeaderModel.getJumlahCart). */
    public void refreshCartCount() {
        // TODO(cart slice)
    }

    /**
     * Adds a unit product to the cart (legacy ProdukModel.addToCart: Keranjang/saveCart with cartCode,
     * jenisProduk=nonpaket, the delivery schedule, idUser when signed in) and refreshes the count on success.
     * ERROR when the request failed or no delivery schedule is chosen.
     */
    public void addToCart(String idProduk, String idSatuan, String quantity, @Nullable String idPromo,
                          ResultCallback<AddToCartResult> callback) {
        // TODO(cart slice)
        callback.onResult(Resource.error("not implemented", 0));
    }

    /**
     * Legacy TransaksiModel.removeTransactionSession(): clears the cart code and used voucher, the saved and
     * copied delivery selection, empties the cart on the server, sets the count to 0. SUCCESS(true) when done.
     */
    public void clearTransactionSession(ResultCallback<Boolean> callback) {
        // TODO(cart slice)
        callback.onResult(Resource.success(true));
    }
}
