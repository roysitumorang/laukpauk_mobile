package id.laukpauk.data.repository;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.payment.CheckoutResponse;
import id.laukpauk.data.model.payment.PaymentChannel;
import id.laukpauk.data.remote.PaymentApi;
import id.laukpauk.util.Json;

/** Payment methods and placing the order (legacy PaymentChannelModel, PembayaranModel.buatOrder). */
public class PaymentRepository extends BaseRepository {

    private final PaymentApi api;

    public PaymentRepository(RepositoryDeps deps) {
        super(deps);
        this.api = api(PaymentApi.class);
    }

    /** The payment channels (empty list on failure). */
    public LiveData<Resource<List<PaymentChannel>>> getPaymentChannels() {
        return load(api.getPaymentChannels(authToken()),
                body -> body == null || body.paymentChannels == null ? new ArrayList<PaymentChannel>()
                        : body.paymentChannels);
    }

    /**
     * POST Transaksi/checkout. The callback always gets SUCCESS: on any failure its data is
     * {statusCheckout: false, message: "Nande"} like the legacy buatOrder.
     */
    public void buatOrder(PaymentChannel channel, Map<String, String> checkoutData, String moreData,
                          ResultCallback<CheckoutResponse> callback) {
        Map<String, String> body = new LinkedHashMap<>(checkoutData);
        body.put("metodePembayaran", channel.channel_code == null ? "" : channel.channel_code);
        body.put("namaMetodePembayaran", channel.name == null ? "" : channel.name);
        body.put("moreDataMetodePembayaran", moreData == null ? "" : moreData);
        String alamat = Json.strOrNull(session.getSavedLatitudeAndLongitude(), "alamat");
        body.put("alamatDelivery", alamat == null ? "" : alamat);
        enqueue(api.checkout(authToken(), body), response -> response, (Resource<CheckoutResponse> res) ->
                callback.onResult(Resource.success(res.isSuccess() && res.data != null
                        ? res.data : CheckoutResponse.failed())));
    }
}
