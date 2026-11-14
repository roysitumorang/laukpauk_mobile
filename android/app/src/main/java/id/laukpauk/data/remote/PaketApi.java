package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.cart.AddToCartResult;
import id.laukpauk.data.model.product.PaketResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Paket endpoints (JSON; ApiClient adds the auth and version headers). */
public interface PaketApi {

    @GET("Produk/detailPaket")
    Call<PaketResponses.Detail> detailPaket(@Query("idPaket") String idPaket);

    @GET("Produk/getPaketLainnya")
    Call<PaketResponses.LainnyaList> getPaketLainnya(@Query("idPaket") String idPaket);

    /** Legacy PaketModel.addToCart (jenisProduk=paket, quantity=1). */
    @POST("Keranjang/saveCart")
    Call<AddToCartResult> saveCart(@Body Map<String, Object> body);
}
