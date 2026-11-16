package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.cart.AddToCartResult;
import id.laukpauk.data.model.product.PackResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Paket endpoints (JSON; ApiClient adds the auth and version headers). */
public interface PackApi {

    @GET("Produk/detailPaket")
    Call<PackResponses.Detail> packDetail(@Query("idPaket") String packId);

    @GET("Produk/getPaketLainnya")
    Call<PackResponses.OthersList> getOtherPacks(@Query("idPaket") String packId);

    /** Legacy PaketModel.addToCart (productType=paket, quantity=1). */
    @POST("Keranjang/saveCart")
    Call<AddToCartResult> saveCart(@Body Map<String, Object> body);
}
