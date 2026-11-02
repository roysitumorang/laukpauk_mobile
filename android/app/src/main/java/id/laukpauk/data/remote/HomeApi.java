package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import id.laukpauk.data.model.home.HomeResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Home page / market / konten / category endpoints (versionCode is added by ApiClient). */
public interface HomeApi {

    @GET("Banner/getBanner")
    Call<HomeResponses.Banners> getBanner();

    @GET("Artikel/getArtikel")
    Call<HomeResponses.Artikels> getArtikel();

    @GET("User/getJumlahVoucher")
    Call<HomeResponses.JumlahVoucher> getJumlahVoucher(@Query("authToken") String authToken);

    @GET("User/getTotalDompet")
    Call<HomeResponses.TotalDompet> getTotalDompet(@Query("authToken") String authToken);

    @GET("Point/getPoint")
    Call<HomeResponses.Point> getPoint(@Query("authToken") String authToken, @Query("point") String point);

    @GET("Pasar/getPasar")
    Call<HomeResponses.Pasars> getPasar(@Nullable @Query("limitPasar") Integer limitPasar,
                                        @Nullable @Query("namaPasar") String namaPasar);

    @GET("Pasar/getPasarAroundOfThisLatLong")
    Call<HomeResponses.Pasars> getPasarAround(@Query("latitude") String latitude,
                                              @Query("longitude") String longitude,
                                              @Query("jadwalDeliveryCode") String jadwalDeliveryCode,
                                              @Nullable @Query("limitPasar") Integer limitPasar,
                                              @Nullable @Query("namaPasar") String namaPasar);

    @GET("Pasar/getProdukInThisPasar/{idPasar}")
    Call<HomeResponses.ProdukPasar> getProdukInThisPasar(@Path("idPasar") String idPasar,
                                                         @Nullable @Query("jadwalDeliveryCode") String jadwalDeliveryCode,
                                                         @Query("startFrom") String startFrom,
                                                         @Query("useLimit") String useLimit,
                                                         @Query("authToken") String authToken);

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
