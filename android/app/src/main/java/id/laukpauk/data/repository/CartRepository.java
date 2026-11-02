package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.cart.AddToCartResult;
import id.laukpauk.data.model.cart.CartGift;
import id.laukpauk.data.model.cart.CartResponses;
import id.laukpauk.data.model.cart.CartSeller;
import id.laukpauk.data.model.cart.WalletConfiguration;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.remote.CartApi;

/**
 * The shopping cart (legacy CheckoutModel cart parts, ProdukModel.addToCart, HeaderModel.getJumlahCart,
 * TransaksiModel.removeTransactionSession, AppState.jumlahCart, GiftModel / GiftTransaksiModel).
 *
 * The first block of public methods is a CONTRACT used by other slices (header badge, product buy sheet,
 * checkout, payment results); the rest serves the cart screens.
 */
public class CartRepository extends BaseRepository {

    private final CartApi api;
    private final DeliveryRepository delivery;
    private final MutableLiveData<Integer> cartCount = new MutableLiveData<>(0);

    public CartRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(CartApi.class);
        this.delivery = delivery;
    }

    /** Number of items in the cart (header badge). */
    public LiveData<Integer> cartCount() {
        return cartCount;
    }

    /** Reloads {@link #cartCount()} from the server (legacy HeaderModel.getJumlahCart; 0 on failure). */
    public void refreshCartCount() {
        enqueue(api.getJumlahCart(session.getCartCode()), body -> body == null ? 0 : body.jumlahKeranjang,
                r -> cartCount.setValue(r.isSuccess() && r.data != null ? r.data : 0));
    }

    /**
     * Adds a unit product to the cart (legacy ProdukModel.addToCart: Keranjang/saveCart with cartCode,
     * jenisProduk=nonpaket, the delivery schedule, idUser when signed in) and refreshes the count on success.
     * ERROR when the request failed or no delivery schedule is chosen.
     */
    public void addToCart(String idProduk, String idSatuan, String quantity, @Nullable String idPromo,
                          ResultCallback<AddToCartResult> callback) {
        DeliverySelection d = delivery.current();
        if (d.jadwalDeliveryCode == null) {
            callback.onResult(Resource.error("Jadwal pengantaran belum dipilih", 0));
            return;
        }
        Map<String, String> cartData = new LinkedHashMap<>();
        cartData.put("cartCode", session.getCartCode());
        cartData.put("idProduk", idProduk);
        cartData.put("jenisProduk", "nonpaket");
        cartData.put("idSatuan", idSatuan);
        cartData.put("quantity", quantity);
        cartData.put("jadwalDeliveryCode", d.jadwalDeliveryCode);
        cartData.put("dayDelivery", d.dayDelivery != null ? d.dayDelivery : "Hari Pengantaran");
        if (idPromo != null) {
            cartData.put("idPromo", idPromo);
        }
        if (!authToken().isEmpty()) {
            cartData.put("idUser", session.getCurrentUserID());
        }
        enqueue(api.saveCart(cartData), body -> body, result -> {
            if (result.isSuccess() && result.data != null && result.data.statusAddCart) {
                refreshCartCount();
            }
            callback.onResult(result);
        });
    }

    /**
     * Legacy TransaksiModel.removeTransactionSession(): clears the cart code and used voucher, the saved and
     * copied delivery selection, empties the cart on the server, sets the count to 0. SUCCESS(true) when done.
     */
    public void clearTransactionSession(ResultCallback<Boolean> callback) {
        session.clearCartCode();
        session.clearUsedVoucher();
        delivery.clear();
        delivery.clearCopied();
        // Like the legacy order of operations: the server call uses the (freshly generated) cart code.
        enqueue(api.bersihkanKeranjang(session.getCartCode()), body -> body != null && body.statusBersihkanKeranjang,
                r -> {
                    cartCount.setValue(0);
                    callback.onResult(Resource.success(true));
                });
    }

    // ---- cart screens --------------------------------------------------------------------------

    /** Cart grouped per seller (legacy getKeranjangPerPenjual; empty list when nothing / on failure). */
    public LiveData<Resource<List<CartSeller>>> cartPerSeller() {
        MutableLiveData<Resource<List<CartSeller>>> live = new MutableLiveData<>(Resource.loading());
        enqueue(api.getCartDividedBySeller(session.getCartCode()),
                body -> body == null || body.listCart == null ? new ArrayList<CartSeller>() : body.listCart,
                r -> live.setValue(r.isError() ? Resource.success(new ArrayList<CartSeller>()) : r));
        return live;
    }

    /** SUBTOTAL (legacy CheckoutModel.getTotalKeranjang). */
    public LiveData<Resource<Integer>> totalKeranjang() {
        DeliverySelection d = delivery.current();
        return load(api.getTotalKeranjang(session.getCartCode(), d.latitude == null ? "" : d.latitude,
                d.longitude == null ? "" : d.longitude), body -> body == null ? 0 : body.totalKeranjang);
    }

    /** Wallet discount configuration; SUCCESS with null data when the server has none. */
    public LiveData<Resource<WalletConfiguration>> walletConfiguration() {
        return load(api.getWalletConfiguration(), body -> body == null ? null : body.walletConfiguration);
    }

    /** Gifts earned by the current cart (gift-transaksi + the details of each existing gift). */
    public LiveData<Resource<List<CartGift>>> gifts() {
        MutableLiveData<Resource<List<CartGift>>> live = new MutableLiveData<>(Resource.loading());
        enqueue(api.getGiftTransaksi(session.getCartCode()),
                body -> body == null || body.giftTransaksi == null
                        ? Collections.<CartResponses.GiftTransaksi>emptyList() : body.giftTransaksi,
                r -> {
                    if (!r.isSuccess() || r.data == null || r.data.isEmpty()) {
                        live.setValue(Resource.success(new ArrayList<CartGift>()));
                        return;
                    }
                    final int total = r.data.size();
                    final CartGift[] found = new CartGift[total];
                    final int[] pending = {total};
                    for (int i = 0; i < total; i++) {
                        final int index = i;
                        enqueue(api.getGift(r.data.get(i).idGift), body -> body == null ? null : body.gift, g -> {
                            found[index] = g.isSuccess() ? g.data : null;
                            if (--pending[0] == 0) {
                                List<CartGift> list = new ArrayList<>();
                                for (CartGift gift : found) {
                                    if (gift != null) {
                                        list.add(gift);
                                    }
                                }
                                live.setValue(Resource.success(list));
                            }
                        });
                    }
                });
        return live;
    }

    /** Saves quantity and note of a cart line (legacy EditKeranjangItem.simpanPerubahan); data = statusAddCart. */
    public void updateItem(String idCart, String quantity, String catatan, ResultCallback<Boolean> callback) {
        enqueue(api.updateCart(idCart, quantity, catatan), body -> body != null && body.statusAddCart, callback);
    }

    /** Deletes a cart line; on success the count is refreshed. data = statusDeleteCart. */
    public void deleteItem(String idCart, ResultCallback<Boolean> callback) {
        enqueue(api.deleteCart(idCart, new HashMap<String, String>()), body -> body != null && body.statusDeleteCart,
                r -> {
                    if (r.isSuccess() && Boolean.TRUE.equals(r.data)) {
                        refreshCartCount();
                    }
                    callback.onResult(r);
                });
    }

    /** "Bersihkan Keranjang": empties the server cart, then refreshes the count. data = status. */
    public void clearCart(ResultCallback<Boolean> callback) {
        enqueue(api.bersihkanKeranjang(session.getCartCode()), body -> body != null && body.statusBersihkanKeranjang,
                r -> {
                    refreshCartCount();
                    callback.onResult(r);
                });
    }
}
