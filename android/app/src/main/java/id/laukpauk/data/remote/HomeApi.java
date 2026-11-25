package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import id.laukpauk.data.model.home.HomeResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Home page / product list / konten / category endpoints (JSON; ApiClient adds the auth and version headers). */
public interface HomeApi {

    /** Hero banners; the list is in {@code data}. */
    @GET("buyer/banners")
    Call<HomeResponses.Banners> getBanner();

    @GET("Konten/getKonten")
    Call<HomeResponses.ContentList> getContent(@Query("page") String page);

    @GET("Konten/getKontenItemFromThisKonten/{idKonten}")
    Call<HomeResponses.ContentItems> getContentItem(@Path("idKonten") String contentId,
                                                  @Nullable @Query("limit") Integer limit);

    @GET("KategoriProduk/getKategoriProduk")
    Call<HomeResponses.CategoryList> getProductCategory();

    @GET("Produk/getProductsInThisCategory/{idKategoriProduk}")
    Call<HomeResponses.ProductInCategory> getProductInThisCategory(@Path("idKategoriProduk") String productCategoryId);

    @GET("Produk/getLowestPriceOfThisProduct/{idProduk}")
    Call<HomeResponses.LowestPriceResponse> getLowestPrice(@Path("idProduk") String productId);
}
