package id.laukpauk.data.remote;

import id.laukpauk.data.model.product.ProdukResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Product endpoints (JSON; ApiClient adds the auth and version headers). */
public interface ProdukApi {

    /** The product list, a page of 10 at a time ({@code useLimit} "y"), optionally filtered by name. */
    @GET("Produk/getProduk")
    Call<ProdukResponses.ProdukList> getProduk(@Query("jadwalDeliveryCode") String jadwalDeliveryCode,
                                               @Query("startFrom") String startFrom,
                                               @Query("useLimit") String useLimit,
                                               @Query("cariProduk") String cariProduk);

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
