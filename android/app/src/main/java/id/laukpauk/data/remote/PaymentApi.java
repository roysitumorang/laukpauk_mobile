package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.payment.CheckoutResponse;
import id.laukpauk.data.model.payment.PaymentChannelsResponse;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Payment endpoints (versionCode is added by ApiClient). */
public interface PaymentApi {

    @GET("Xendit/getPaymentChannels")
    Call<PaymentChannelsResponse> getPaymentChannels(@Query("authToken") String authToken);

    @FormUrlEncoded
    @POST("Transaksi/checkout")
    Call<CheckoutResponse> checkout(@Query("authToken") String authToken, @FieldMap Map<String, String> body);
}
