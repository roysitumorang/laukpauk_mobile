package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;

/** Port of lib/models/KodePromoModel.dart. */
public final class KodePromoModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public KodePromoModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /** Dart: cekKodePromo({required kodePromo}) -> Future&lt;Map?&gt;. BLOCKING. Null when the request failed. */
    public JsonObject cekKodePromo(String kodePromo) {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi(
                "/Promo/cekKodePromo/" + ShopModelUi.pathEnc(kodePromo) + "?" + params);

        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject();
        }

        return null;
    }
}
