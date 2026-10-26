package id.laukpauk.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;

/** Port of lib/models/PointModel.dart. */
public final class PointModel {
    private PointModel() {}

    public static final String createdFrom_admin = "admin";
    public static final String createdFrom_transaksi = "transaksi";

    /**
     * Dart: getPoint({String? queryString}) -> Future&lt;dynamic&gt;. BLOCKING. `queryString` may
     * be null. Dart quirk kept: a given queryString REPLACES the default
     * 'historiPoint=true&amp;point=true' (it is not appended). Returns the `response` member, or false.
     */
    public static JsonElement getPoint(String queryString) {
        String queryStringParams = "historiPoint=true&point=true";
        if (queryString != null) {
            queryStringParams = "&" + queryString;
        }

        String params = HTTPRequest.getQueryString(queryStringParams, true);

        Http.Response httpResponse = Http.getApi("/Point/getPoint?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject body = httpResponse.jsonObject();
            return ShopModelUi.dyn(body.get("response"));
        }

        return ShopModelUi.FALSE;
    }

    public static JsonElement getPoint() {
        return getPoint(null);
    }
}
