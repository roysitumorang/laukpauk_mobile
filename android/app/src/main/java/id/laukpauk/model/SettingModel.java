package id.laukpauk.model;

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
    private SettingModel() {}

    public static JsonElement getSettingValue(String keyPengaturan) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Pengaturan/getSettingValue/" + keyPengaturan + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return valueOrNull(Json.get(dataObject, "settingValue"));
        }
        return new JsonPrimitive(false);
    }

    public static JsonElement getContactUsWhatsappConfiguration() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Pengaturan/getContactUsWhatsappConfiguration?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return valueOrNull(Json.get(dataObject, "contactUsWhatsappConfiguration"));
        }
        return new JsonPrimitive(false);
    }

    private static JsonElement valueOrNull(JsonElement e) {
        return Json.isNull(e) ? null : e;
    }
}
