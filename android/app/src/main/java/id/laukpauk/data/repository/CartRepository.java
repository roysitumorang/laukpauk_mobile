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
import id.laukpauk.data.model.cart.BuyerCart;
import id.laukpauk.data.model.cart.BuyerCartItem;
import id.laukpauk.data.model.cart.CartGift;
import id.laukpauk.data.model.cart.CartProduct;
import id.laukpauk.data.model.cart.CartResponses;
import id.laukpauk.data.model.cart.WalletConfiguration;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.remote.CartApi;

/**
 * The shopping cart (legacy CheckoutModel cart parts, ProdukModel.addToCart, HeaderModel.getCartQuantity,
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

    /** Reloads {@link #cartCount()} from the server (legacy HeaderModel.getCartQuantity; 0 on failure). */
    public void refreshCartCount() {
        enqueue(api.getCartQuantity(session.getCartCode()), body -> body == null ? 0 : body.cartCount,
                r -> cartCount.setValue(r.isSuccess() && r.data != null ? r.data : 0));
    }

    /**
     * Adds {@code quantity} of a product variant to the signed-in buyer's cart. {@code POST buyer/cart} replaces
     * the whole cart, so the current cart is read first ({@code GET buyer/cart}) and sent back with the variant's
     * quantity increased (or the variant appended). On success the header badge shows the cart's total quantity.
     */
    public void addVariant(String variantId, long quantity, ResultCallback<BuyerCart> callback) {
        enqueue(api.buyerCart(), (CartResponses.BuyerCartEnvelope body) -> body == null ? null : body.data, current -> {
            if (!current.isSuccess()) {
                callback.onResult(Resource.<BuyerCart>error(current.message, current.code));
                return;
            }
            enqueue(api.saveBuyerCart(cartBody(current.data, variantId, quantity)),
                    (CartResponses.BuyerCartEnvelope body) -> body == null ? null : body.data, saved -> {
                        if (saved.isSuccess() && saved.data != null) {
                            cartCount.setValue((int) Math.min(Integer.MAX_VALUE, saved.data.quantity));
                        }
                        callback.onResult(saved);
                    });
        });
    }

    /** The {@code POST buyer/cart} body: the cart's items with {@code quantity} more of {@code variantId}. */
    static Map<String, Object> cartBody(@Nullable BuyerCart cart, String variantId, long quantity) {
        List<Map<String, Object>> items = new ArrayList<>();
        boolean found = false;
        if (cart != null && cart.items != null) {
            for (BuyerCartItem item : cart.items) {
                if (item == null || item.productVariantId == null || item.quantity <= 0) {
                    continue;
                }
                long q = item.quantity;
                if (item.productVariantId.equals(variantId)) {
                    q += quantity;
                    found = true;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("product_variant_id", item.productVariantId);
                row.put("quantity", q);
                items.add(row);
            }
        }
        if (!found) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product_variant_id", variantId);
            row.put("quantity", quantity);
            items.add(row);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        if (cart != null && cart.id != null && !cart.id.isEmpty() && !cart.id.matches("[0-]+")) {
            body.put("id", cart.id);
        }
        body.put("items", items);
        return body;
    }

    /**
     * Adds a unit product to the cart (legacy ProdukModel.addToCart: Keranjang/saveCart with cartCode,
     * productType=nonpaket, the delivery schedule when one is chosen, idUser when signed in) and refreshes the
     * count on success. No delivery address or schedule is needed to add to the cart; checkout asks for it.
     */
    public void addToCart(String productId, String unitId, String quantity, @Nullable String idPromo,
                          ResultCallback<AddToCartResult> callback) {
        DeliverySelection d = delivery.current();
        Map<String, String> cartData = new LinkedHashMap<>();
        cartData.put("cartCode", session.getCartCode());
        cartData.put("idProduk", productId);
        cartData.put("jenisProduk", "nonpaket");
        cartData.put("idSatuan", unitId);
        cartData.put("quantity", quantity);
        if (d.deliveryScheduleCode != null) {
            cartData.put("jadwalDeliveryCode", d.deliveryScheduleCode);
            cartData.put("dayDelivery", d.dayDelivery != null ? d.dayDelivery : "Hari Pengantaran");
        }
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
        enqueue(api.clearCart(session.getCartCode()), body -> body != null && body.clearCartStatus,
                r -> {
                    cartCount.setValue(0);
                    callback.onResult(Resource.success(true));
                });
    }

    // ---- cart screens --------------------------------------------------------------------------

    /** The cart lines (empty list when nothing / on failure). */
    public LiveData<Resource<List<CartProduct>>> cart() {
        MutableLiveData<Resource<List<CartProduct>>> live = new MutableLiveData<>(Resource.loading());
        enqueue(api.getCart(session.getCartCode()),
                body -> body == null || body.listCart == null ? new ArrayList<CartProduct>() : body.listCart,
                r -> live.setValue(r.isError() ? Resource.success(new ArrayList<CartProduct>()) : r));
        return live;
    }

    /** SUBTOTAL (legacy CheckoutModel.getCartTotal). */
    public LiveData<Resource<Integer>> cartTotal() {
        DeliverySelection d = delivery.current();
        return load(api.getCartTotal(session.getCartCode(), d.latitude == null ? "" : d.latitude,
                d.longitude == null ? "" : d.longitude), body -> body == null ? 0 : body.cartTotal);
    }

    /** Wallet discount configuration; SUCCESS with null data when the server has none. */
    public LiveData<Resource<WalletConfiguration>> walletConfiguration() {
        return load(api.getWalletConfiguration(), body -> body == null ? null : body.walletConfiguration);
    }

    /** Gifts earned by the current cart (gift-transaksi + the details of each existing gift). */
    public LiveData<Resource<List<CartGift>>> gifts() {
        MutableLiveData<Resource<List<CartGift>>> live = new MutableLiveData<>(Resource.loading());
        enqueue(api.getTransactionGift(session.getCartCode()),
                body -> body == null || body.transactionGift == null
                        ? Collections.<CartResponses.TransactionGift>emptyList() : body.transactionGift,
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
    public void updateItem(String idCart, String quantity, String note, ResultCallback<Boolean> callback) {
        enqueue(api.updateCart(idCart, body("quantity", quantity, "catatan", note)),
                body -> body != null && body.statusAddCart, callback);
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
        enqueue(api.clearCart(session.getCartCode()), body -> body != null && body.clearCartStatus,
                r -> {
                    refreshCartCount();
                    callback.onResult(r);
                });
    }
}
