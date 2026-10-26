package id.laukpauk.model;

import com.google.gson.JsonArray;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/PaymentChannelModel.dart. Blocking (network): call from a background thread. */
public final class PaymentChannelModel {
    private PaymentChannelModel() {}

    /** GET /Xendit/getPaymentChannels -> the "paymentChannels" list ([] on failure). */
    public static JsonArray getPaymentChannels() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Xendit/getPaymentChannels?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "paymentChannels");
        }

        return new JsonArray();
    }
}
