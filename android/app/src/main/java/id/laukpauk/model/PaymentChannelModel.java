package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.util.Json;

/** Port of lib/models/PaymentChannelModel.dart. Blocking (network): call from a background thread. */
public final class PaymentChannelModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public PaymentChannelModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /** GET /Xendit/getPaymentChannels -> the "paymentChannels" list ([] on failure). */
    public JsonArray getPaymentChannels() {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/Xendit/getPaymentChannels?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "paymentChannels");
        }

        return new JsonArray();
    }
}
