package id.laukpauk.data.model.checkout;

import com.squareup.moshi.Json;


import androidx.annotation.Nullable;


/**
 * Everything the Checkout screen needs once the cart total is known (legacy
 * CheckoutActivity._dataInitialization). Assembled by CheckoutRepository from several requests.
 */
public class CheckoutInit {
    /** shoppingTotal > 0 (the buyer cart's total); false = nothing to check out. */
    public boolean enough;
    /** The delivery cost is known (the delivery address and {@code buyer/setting} arrived). */
    public boolean complete;
    @Json(name = "totalBelanja")
    public long shoppingTotal;
    /** Delivery cost: {@link #distanceKm} × {@link #deliveryCostPerKm}. */
    @Json(name = "totalOngkir")
    public long totalShippingFee;
    /** {@code distance_km} of the delivery address. */
    @Json(name = "distance_km")
    public long distanceKm;
    /** {@code delivery_cost_per_km} of {@code GET buyer/setting}. */
    @Json(name = "delivery_cost_per_km")
    public long deliveryCostPerKm;
    /** {@code id} of {@code GET buyer/cart}. */
    @Json(name = "cart_id")
    @Nullable public String cartId;
    /** The cart lines of {@code GET buyer/cart} (their ids and notes go with the order). */
    @Json(name = "cart_items")
    @Nullable public java.util.List<id.laukpauk.data.model.cart.BuyerCartItem> cartItems;
    /** {@code id} of the chosen delivery address. */
    @Json(name = "delivery_address_id")
    @Nullable public String deliveryAddressId;
    /** {@code voucher_enabled} of {@code GET buyer/setting}. */
    @Json(name = "voucher_enabled")
    public boolean voucherEnabled;
    @Json(name = "jumlahBarang")
    public long goodsCount;
    @Json(name = "totalDompetSaya")
    public long myWalletTotal;
    public long point;
    @Json(name = "minimalPointTukarPoint")
    public long minimumPointRedeem;
    public String cartCode = "";
    @Json(name = "penjelasanOngkir")
    public String shippingFeeExplanation = "";
    @Json(name = "nama")
    public String name = "";
    @Json(name = "nomorHP")
    public String phoneNumber = "";
    @Json(name = "alamat")
    public String address = "";
}
