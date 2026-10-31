package id.laukpauk.model;

import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;

/** Port of lib/models/KodePromoModel.dart. */
public final class KodePromoModel {
    private KodePromoModel() {}

    /** Dart: cekKodePromo({required kodePromo}) -> Future&lt;Map?&gt;. BLOCKING. Null when the request failed. */
    public static JsonObject cekKodePromo(String kodePromo) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi(
                "/Promo/cekKodePromo/" + ShopModelUi.pathEnc(kodePromo) + "?" + params);

        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject();
        }

        return null;
    }
}
