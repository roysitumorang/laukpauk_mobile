package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/**
 * Port of lib/models/SettingModel.dart. BLOCKING (network). Both methods return Dart's `dynamic`:
 * the server value, or the boolean `false` (a JsonPrimitive) when the request failed. A server
 * value of JSON null is returned as Java null.
 */
public final class SettingModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public SettingModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    public JsonElement getSettingValue(String keyPengaturan) {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/Pengaturan/getSettingValue/" + keyPengaturan + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return valueOrNull(Json.get(dataObject, "settingValue"));
        }
        return new JsonPrimitive(false);
    }

    public JsonElement getContactUsWhatsappConfiguration() {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/Pengaturan/getContactUsWhatsappConfiguration?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return valueOrNull(Json.get(dataObject, "contactUsWhatsappConfiguration"));
        }
        return new JsonPrimitive(false);
    }

    private JsonElement valueOrNull(JsonElement e) {
        return Json.isNull(e) ? null : e;
    }
}
