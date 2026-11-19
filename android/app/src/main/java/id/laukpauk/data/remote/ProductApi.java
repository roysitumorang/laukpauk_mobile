package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import id.laukpauk.data.model.product.ProductResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/** Product endpoints (JSON; ApiClient adds the auth and version headers). */
public interface ProductApi {

    /** The first page of products ({@code limit} per page), filtered by {@code q} when not null. */
    @GET("buyer/products")
    Call<ProductResponses.BuyerProducts> buyerProducts(@Query("limit") int limit, @Nullable @Query("q") String q);

    /** A next page: {@code data.next} of the previous page, a full URL. */
    @GET
    Call<ProductResponses.BuyerProducts> buyerProductsPage(@Url String next);

    @GET("Produk/detailProduk")
    Call<ProductResponses.Detail> productDetail(@Query("idProduk") String productId);

    @GET("Produk/getLowestPriceOfThisProduct/{idProduk}")
    Call<ProductResponses.LowestPriceResponse> getLowestPrice(@Path("idProduk") String productId);

    @GET("Produk/getHargaProduk")
    Call<ProductResponses.PriceList> getProductPrice(@Query("idProduk") String productId);

    @GET("Produk/getProdukLainnya")
    Call<ProductResponses.OthersList> getOtherProducts(@Query("idProduk") String productId);

    @GET("Produk/isThisProductInCA")
    Call<ProductResponses.InCa> isThisProductInCA(@Query("idProduk") String productId,
                                                 @Query("latitude") String latitude,
                                                 @Query("longitude") String longitude);
}
