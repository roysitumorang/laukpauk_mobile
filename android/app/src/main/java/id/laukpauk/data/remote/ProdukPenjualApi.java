package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.seller.products.ProdukPenjualResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Seller product management endpoints (versionCode is added by ApiClient). */
public interface ProdukPenjualApi {

    /** Detail of a product; with {@code entitasHargaProduk} it also returns its units ({@code hargaProduk}). */
    @GET("Produk/getHargaProduk")
    Call<ProdukPenjualResponses.HargaProduk> getHargaProduk(@Query("idProduk") String idProduk,
                                                           @Query("entitasHargaProduk") String entitasHargaProduk);

    @GET("Produk/detailProduk")
    Call<ProdukPenjualResponses.DetailProduk> getDetailProduk(@Query("idProduk") String idProduk);

    @GET("Produk/detailPaket")
    Call<ProdukPenjualResponses.DetailPaket> getDetailPaket(@Query("idPaket") String idPaket);

    @GET("KategoriProduk/getKategoriProduk")
    Call<ProdukPenjualResponses.Kategori> getKategoriProduk();

    @GET("Penjual/getProdukPenjual")
    Call<ProdukPenjualResponses.ProdukList> getProdukPenjual(@Query("authToken") String authToken,
                                                            @Query("getProductEntities") String entities,
                                                            @Query("term") String term);

    @GET("Penjual/getPaketPenjual")
    Call<ProdukPenjualResponses.PaketList> getPaketPenjual(@Query("authToken") String authToken);

    @GET("Produk/toggleProdukPalingLaris/{id}")
    Call<ProdukPenjualResponses.Status> toggleProdukPalingLaris(@Path("id") String id,
                                                               @Query("authToken") String authToken);

    @GET("Produk/togglePaketPalingLaris/{id}")
    Call<ProdukPenjualResponses.Status> togglePaketPalingLaris(@Path("id") String id,
                                                              @Query("authToken") String authToken);

    @FormUrlEncoded
    @POST("Produk/ubahKategoriProduk/{id}")
    Call<ProdukPenjualResponses.Status> ubahKategoriProduk(@Path("id") String id,
                                                          @Query("authToken") String authToken,
                                                          @Field("kategoriProduk") String kategoriProduk);

    @FormUrlEncoded
    @POST("Produk/ubahDeskripsiProduk/{id}")
    Call<ProdukPenjualResponses.Status> ubahDeskripsiProduk(@Path("id") String id,
                                                           @Query("authToken") String authToken,
                                                           @Field("deskripsiProduk") String deskripsi);

    @FormUrlEncoded
    @POST("Produk/ubahDeskripsiPaket/{id}")
    Call<ProdukPenjualResponses.Status> ubahDeskripsiPaket(@Path("id") String id,
                                                          @Query("authToken") String authToken,
                                                          @Field("deskripsiPaket") String deskripsi);

    @GET("Produk/hapusProduk/{id}")
    Call<ProdukPenjualResponses.Status> hapusProduk(@Path("id") String id, @Query("authToken") String authToken);

    @GET("Produk/hapusPaket/{id}")
    Call<ProdukPenjualResponses.Status> hapusPaket(@Path("id") String id, @Query("authToken") String authToken);

    /**
     * New product. Fields: nama, jenisProduk, keterangan, produkImg/paketImg (base64 image, optional),
     * kategori (satuan) or harga + diskon (paket). The server expects the image as a base64 form field.
     */
    @FormUrlEncoded
    @POST("Produk/saveProduk")
    Call<ProdukPenjualResponses.Status> saveProduk(@Query("authToken") String authToken,
                                                  @FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("Produk/saveProduk/{id}")
    Call<ProdukPenjualResponses.Status> saveProduk(@Path("id") String id, @Query("authToken") String authToken,
                                                  @FieldMap Map<String, String> fields);

    @GET("HargaProduk/hapusHargaProduk/{id}")
    Call<ProdukPenjualResponses.Status> hapusHargaProduk(@Path("id") String id,
                                                        @Query("authToken") String authToken);

    /** Fields: idProduk, nama, harga, diskon, jenisHarga, keterangan. */
    @FormUrlEncoded
    @POST("HargaProduk/saveHargaProduk")
    Call<ProdukPenjualResponses.Status> saveHargaProduk(@Query("authToken") String authToken,
                                                       @FieldMap Map<String, String> fields);

    @FormUrlEncoded
    @POST("HargaProduk/saveHargaProduk/{id}")
    Call<ProdukPenjualResponses.Status> saveHargaProduk(@Path("id") String id,
                                                       @Query("authToken") String authToken,
                                                       @FieldMap Map<String, String> fields);
}
