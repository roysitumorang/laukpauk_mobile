package id.laukpauk.data.remote;

import id.laukpauk.data.model.cart.AddToCartResult;
import id.laukpauk.data.model.product.PaketResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Paket endpoints (versionCode is added by ApiClient). */
public interface PaketApi {

    @GET("Produk/detailPaket")
    Call<PaketResponses.Detail> detailPaket(@Query("idPaket") String idPaket);

    @GET("Produk/getPaketLainnya")
    Call<PaketResponses.LainnyaList> getPaketLainnya(@Query("idPaket") String idPaket);

    /** Legacy PaketModel.addToCart (jenisProduk=paket, quantity=1). */
    @FormUrlEncoded
    @POST("Keranjang/saveCart")
    Call<AddToCartResult> saveCart(@Field("cartCode") String cartCode,
                                   @Field("idProduk") String idPaket,
                                   @Field("jenisProduk") String jenisProduk,
                                   @Field("quantity") String quantity,
                                   @Field("jadwalDeliveryCode") String jadwalDeliveryCode,
                                   @Field("dayDelivery") String dayDelivery);
}
