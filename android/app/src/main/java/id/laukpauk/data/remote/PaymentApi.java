package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.payment.CheckoutResponse;
import id.laukpauk.data.model.payment.PaymentChannelsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/** Payment endpoints (JSON; ApiClient adds the auth and version headers). */
public interface PaymentApi {

    @GET("Xendit/getPaymentChannels")
    Call<PaymentChannelsResponse> getPaymentChannels();

    @POST("Transaksi/checkout")
    Call<CheckoutResponse> checkout(@Body Map<String, String> body);
}
