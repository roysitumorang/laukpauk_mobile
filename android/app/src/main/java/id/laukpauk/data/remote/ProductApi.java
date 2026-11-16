package id.laukpauk.data.remote;

import id.laukpauk.data.model.product.ProductResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Product endpoints (JSON; ApiClient adds the auth and version headers). */
public interface ProductApi {

    /** The product list, a page of 10 at a time ({@code useLimit} "y"), optionally filtered by name. */
    @GET("Produk/getProduk")
    Call<ProductResponses.ProductList> getProduct(@Query("jadwalDeliveryCode") String deliveryScheduleCode,
                                               @Query("startFrom") String startFrom,
                                               @Query("useLimit") String useLimit,
                                               @Query("cariProduk") String searchProduct);

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
