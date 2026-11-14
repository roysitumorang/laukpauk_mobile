package id.laukpauk.data.remote;

import id.laukpauk.data.model.product.ProdukResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Product endpoints (versionCode is added by ApiClient). */
public interface ProdukApi {

    @GET("Produk/detailProduk")
    Call<ProdukResponses.Detail> detailProduk(@Query("idProduk") String idProduk);

    @GET("Produk/getLowestPriceOfThisProduct/{idProduk}")
    Call<ProdukResponses.LowestPriceResponse> getLowestPrice(@Path("idProduk") String idProduk);

    @GET("Produk/getHargaProduk")
    Call<ProdukResponses.HargaList> getHargaProduk(@Query("idProduk") String idProduk);

    @GET("Produk/getProdukLainnya")
    Call<ProdukResponses.LainnyaList> getProdukLainnya(@Query("idProduk") String idProduk);

    @GET("Produk/isThisProductInCA")
    Call<ProdukResponses.InCa> isThisProductInCA(@Query("idProduk") String idProduk,
                                                 @Query("latitude") String latitude,
                                                 @Query("longitude") String longitude);
}
