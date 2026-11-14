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

    @GET("User/getJumlahCart")
    Call<CartResponses.Count> getJumlahCart(@Query("cartCode") String cartCode);

    @GET("User/getCart")
    Call<CartResponses.ProductList> getCart(@Query("cartCode") String cartCode);

    @GET("User/getTotalKeranjang")
    Call<CartResponses.Total> getTotalKeranjang(@Query("cartCode") String cartCode,
                                                @Query("latDelivery") String latDelivery,
                                                @Query("longDelivery") String longDelivery);

    @GET("Keranjang/bersihkanKeranjang")
    Call<CartResponses.Clear> bersihkanKeranjang(@Query("cartCode") String cartCode);

    @POST("Keranjang/saveCart")
    Call<AddToCartResult> saveCart(@Body Map<String, String> fields);

    @POST("Keranjang/saveCart/{idItem}")
    Call<AddToCartResult> updateCart(@Path("idItem") String idItem, @Body Map<String, Object> body);

    /** The body is an empty JSON object. */
    @POST("Keranjang/deleteCart/{idCart}")
    Call<CartResponses.Delete> deleteCart(@Path("idCart") String idCart, @Body Map<String, String> noFields);

    /** Duplicate of the checkout slice's wallet configuration call. */
    @GET("Pengaturan/getWalletConfiguration")
    Call<CartResponses.Wallet> getWalletConfiguration();

    @GET("gift/gift-transaksi")
    Call<CartResponses.GiftTransaksiList> getGiftTransaksi(@Query("cartCode") String cartCode);

    @GET("Gift/getGift/{idGift}")
    Call<CartResponses.GiftDetail> getGift(@Path("idGift") String idGift);
}
