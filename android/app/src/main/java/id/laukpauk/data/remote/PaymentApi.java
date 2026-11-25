package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.payment.PaymentChannelsResponse;
import id.laukpauk.data.model.payment.TransactionResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Payment endpoints (JSON; ApiClient adds the auth and version headers). */
public interface PaymentApi {

    /**
     * The payment channels for this amount (buyer JWT required): the backend lists only the channels whose minimum
     * and maximum amount allow it.
     */
    @GET("buyer/payment_channels")
    Call<PaymentChannelsResponse> buyerPaymentChannels(@Query("amount") long amount);

    /**
     * Places the order (buyer JWT required): 201 with the transaction in {@code data}; 422 with the reason in
     * {@code message}. See {@link id.laukpauk.data.repository.PaymentRepository#transactionBody}.
     */
    @POST("buyer/transactions")
    Call<TransactionResponses.Created> createTransaction(@Body Map<String, Object> body);
}
