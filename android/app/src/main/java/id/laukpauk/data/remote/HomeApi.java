package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import id.laukpauk.data.model.home.HomeResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Home page / product list / konten / category endpoints (JSON; ApiClient adds the auth and version headers). */
public interface HomeApi {

    @GET("Banner/getBanner")
    Call<HomeResponses.Banners> getBanner();

    @GET("Artikel/getArtikel")
    Call<HomeResponses.Artikels> getArtikel();

    @GET("User/getJumlahVoucher")
    Call<HomeResponses.JumlahVoucher> getJumlahVoucher();

    @GET("User/getTotalDompet")
    Call<HomeResponses.TotalDompet> getTotalDompet();

    @GET("Point/getPoint")
    Call<HomeResponses.Point> getPoint(@Query("point") String point);

    /** The first page of the product list (see ProdukApi.getProduk). */
    @GET("Produk/getProduk")
    Call<HomeResponses.ProdukList> getProduk(@Nullable @Query("jadwalDeliveryCode") String jadwalDeliveryCode,
                                             @Query("startFrom") String startFrom,
                                             @Query("useLimit") String useLimit);

    @GET("Konten/getKonten")
    Call<HomeResponses.KontenList> getKonten(@Query("page") String page);

    @GET("Konten/getKontenItemFromThisKonten/{idKonten}")
    Call<HomeResponses.KontenItems> getKontenItem(@Path("idKonten") String idKonten,
                                                  @Nullable @Query("limit") Integer limit);

    @GET("KategoriProduk/getKategoriProduk")
    Call<HomeResponses.KategoriList> getKategoriProduk();

    @GET("Produk/getProductsInThisCategory/{idKategoriProduk}")
    Call<HomeResponses.ProdukInKategori> getProdukInThisCategory(@Path("idKategoriProduk") String idKategoriProduk);

    @GET("Produk/getLowestPriceOfThisProduct/{idProduk}")
    Call<HomeResponses.LowestPriceResponse> getLowestPrice(@Path("idProduk") String idProduk);
}
