package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.cart.AddToCartResult;
import id.laukpauk.data.model.cart.CartResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Cart endpoints (JSON; ApiClient adds the auth and version headers). */
public interface CartApi {

    /** The signed-in buyer's cart (JWT required); an empty cart when there is none yet. */
    @GET("buyer/cart")
    Call<CartResponses.BuyerCartEnvelope> buyerCart();

    /**
     * Saves the buyer's whole cart: {@code {"id": <cart id, optional>, "items": [{"product_variant_id", "quantity"}]}}.
     * The items replace the cart's items (a variant missing from the list is removed); answers the saved cart.
     */
    @POST("buyer/cart")
    Call<CartResponses.BuyerCartEnvelope> saveBuyerCart(@Body Map<String, Object> body);

    @GET("Keranjang/bersihkanKeranjang")
    Call<CartResponses.Clear> clearCart(@Query("cartCode") String cartCode);

    @POST("Keranjang/saveCart")
    Call<AddToCartResult> saveCart(@Body Map<String, String> fields);

    @GET("gift/gift-transaksi")
    Call<CartResponses.TransactionGiftList> getTransactionGift(@Query("cartCode") String cartCode);

    @GET("Gift/getGift/{idGift}")
    Call<CartResponses.GiftDetail> getGift(@Path("idGift") String idGift);
}
