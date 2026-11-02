package id.laukpauk.data.remote;

import id.laukpauk.data.model.orders.RepeatOrderResponses;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Repeat-order endpoints (versionCode is added by ApiClient). */
public interface RepeatOrderApi {

    @GET("RO/getItemTransaksi")
    Call<RepeatOrderResponses.Items> getItemTransaksi(@Query("authToken") String authToken,
                                                      @Query("idTransaksi") String idTransaksi,
                                                      @Query("latDelivery") String latDelivery,
                                                      @Query("longDelivery") String longDelivery,
                                                      @Query("jadwalDeliveryCode") String jadwalDeliveryCode);

    @FormUrlEncoded
    @POST("RO/addToCartThesePenjual")
    Call<RepeatOrderResponses.AddToCart> addToCartThesePenjual(@Query("authToken") String authToken,
                                                               @Field("idTransaksi") String idTransaksi,
                                                               @Field("cartCode") String cartCode,
                                                               @Field("selectedSellers") String selectedSellers);
}
