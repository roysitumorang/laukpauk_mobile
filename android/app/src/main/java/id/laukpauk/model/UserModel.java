package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/UserModel.dart. BLOCKING (network). */
public final class UserModel {
    private UserModel() {}

    /**
     * Dart `dynamic`: the `detailUser` map (a JsonObject), the literal `false` (a JsonPrimitive) when
     * the API says there is no active user or the request failed, or null when the field is missing.
     */
    public static JsonElement getDetailActiveUser() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/User/getDetailUser?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            JsonElement detailUser = Json.get(dataObject, "detailUser");
            return Json.isNull(detailUser) ? null : detailUser;
        }

        return new JsonPrimitive(false);
    }

    public static JsonArray getHistoriPoint() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/User/getRiwayatPoint?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "riwayatPoint");
        }

        return new JsonArray();
    }
}
