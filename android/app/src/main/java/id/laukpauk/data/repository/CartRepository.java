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

    /** Reloads {@link #cartCount()}: the total quantity of {@code GET buyer/cart}; 0 when signed out or on failure. */
    public void refreshCartCount() {
        if (authToken().isEmpty()) {
            cartCount.setValue(0);
            return;
        }
        enqueue(api.buyerCart(), CartRepository::cartOf, r -> setCount(r.isSuccess() ? r.data : null));
    }

    private void setCount(@Nullable BuyerCart cart) {
        cartCount.setValue(cart == null ? 0 : (int) Math.min(Integer.MAX_VALUE, Math.max(0, cart.quantity)));
    }

    @Nullable
    private static BuyerCart cartOf(@Nullable CartResponses.BuyerCartEnvelope body) {
        return body == null ? null : body.data;
    }

    /**
     * The signed-in buyer's cart ({@code GET buyer/cart}) with each item's product and price; an empty cart when
     * signed out (buyers add to the cart only after logging in). Also updates the header badge.
     */
    public LiveData<Resource<BuyerCart>> buyerCart() {
        if (authToken().isEmpty()) {
            cartCount.setValue(0);
            return new MutableLiveData<>(Resource.success(new BuyerCart()));
        }
        MutableLiveData<Resource<BuyerCart>> live = new MutableLiveData<>(Resource.loading());
        enqueue(api.buyerCart(), CartRepository::cartOf, r -> {
            if (r.isSuccess()) {
                setCount(r.data);
            }
            live.setValue(r.isSuccess() && r.data == null ? Resource.success(new BuyerCart()) : r);
        });
        return live;
    }

    /**
     * Adds {@code quantity} of a product variant to the signed-in buyer's cart. {@code POST buyer/cart} replaces
     * the whole cart, so the current cart is read first ({@code GET buyer/cart}) and sent back with the variant's
     * quantity increased (or the variant appended). On success the header badge shows the cart's total quantity.
     */
    public void addVariant(String variantId, long quantity, ResultCallback<BuyerCart> callback) {
        updateCart(cart -> cartBody(cart, variantId, quantity), callback);
    }

    /** Sets a variant's quantity in the cart; 0 removes it. Same read-then-save as {@link #addVariant}. */
    public void setVariantQuantity(String variantId, long quantity, ResultCallback<BuyerCart> callback) {
        updateCart(cart -> quantityBody(cart, variantId, quantity), callback);
    }

    /** Empties the buyer's cart ({@code POST buyer/cart} with no items). */
    public void clearBuyerCart(ResultCallback<BuyerCart> callback) {
        updateCart(cart -> body(cart, new ArrayList<Map<String, Object>>()), callback);
    }

    private interface CartChange {
        Map<String, Object> body(@Nullable BuyerCart current);
    }

    /** {@code GET buyer/cart}, then {@code POST buyer/cart} with the changed items; updates the badge on success. */
    private void updateCart(CartChange change, ResultCallback<BuyerCart> callback) {
        enqueue(api.buyerCart(), CartRepository::cartOf, current -> {
            if (!current.isSuccess()) {
                callback.onResult(Resource.<BuyerCart>error(current.message, current.code));
                return;
            }
            enqueue(api.saveBuyerCart(change.body(current.data)), CartRepository::cartOf, saved -> {
                if (saved.isSuccess()) {
                    setCount(saved.data);
                }
                callback.onResult(saved);
            });
        });
    }

    /** The {@code POST buyer/cart} body: the cart's items with {@code quantity} more of {@code variantId}. */
    static Map<String, Object> cartBody(@Nullable BuyerCart cart, String variantId, long quantity) {
        List<Map<String, Object>> items = new ArrayList<>();
        boolean found = false;
        for (BuyerCartItem item : itemsOf(cart)) {
            long q = item.quantity;
            if (item.productVariantId.equals(variantId)) {
                q += quantity;
                found = true;
            }
            items.add(row(item.productVariantId, q));
        }
        if (!found) {
            items.add(row(variantId, quantity));
        }
        return body(cart, items);
    }

    /** The {@code POST buyer/cart} body with {@code variantId} at {@code quantity}; left out when 0 or less. */
    static Map<String, Object> quantityBody(@Nullable BuyerCart cart, String variantId, long quantity) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (BuyerCartItem item : itemsOf(cart)) {
            long q = item.productVariantId.equals(variantId) ? quantity : item.quantity;
            if (q > 0) {
                items.add(row(item.productVariantId, q));
            }
        }
        return body(cart, items);
    }

    /** The cart's items that can be sent back (a variant id and a positive quantity). */
    private static List<BuyerCartItem> itemsOf(@Nullable BuyerCart cart) {
        List<BuyerCartItem> list = new ArrayList<>();
        if (cart != null && cart.items != null) {
            for (BuyerCartItem item : cart.items) {
                if (item != null && item.productVariantId != null && item.quantity > 0) {
                    list.add(item);
                }
            }
        }
        return list;
    }

    private static Map<String, Object> row(String variantId, long quantity) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("product_variant_id", variantId);
        row.put("quantity", quantity);
        return row;
    }

    /** {@code {"id": <cart id, left out while all zeros>, "items": [...]}}. */
    private static Map<String, Object> body(@Nullable BuyerCart cart, List<Map<String, Object>> items) {
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
}
