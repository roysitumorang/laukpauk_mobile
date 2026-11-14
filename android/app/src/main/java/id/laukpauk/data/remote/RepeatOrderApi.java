package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.orders.RepeatOrderResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Repeat-order endpoints (JSON; ApiClient adds the auth and version headers). */
public interface RepeatOrderApi {

    @GET("RO/getItemTransaksi")
    Call<RepeatOrderResponses.Items> getItemTransaksi(@Query("idTransaksi") String idTransaksi,
                                                      @Query("latDelivery") String latDelivery,
                                                      @Query("longDelivery") String longDelivery,
                                                      @Query("jadwalDeliveryCode") String jadwalDeliveryCode);

    @POST("RO/addToCart")
    Call<RepeatOrderResponses.AddToCart> addToCart(@Body Map<String, Object> body);
}
