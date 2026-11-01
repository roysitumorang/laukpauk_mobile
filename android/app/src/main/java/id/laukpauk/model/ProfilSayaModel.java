package id.laukpauk.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/ProfilSayaModel.dart. Blocking (network): call from a background thread. */
public final class ProfilSayaModel {
    private ProfilSayaModel() {}

    /**
     * GET /User/getDetailUser[?getUserEntities=select]. Dart returned `dynamic`: the "detailUser" value
     * (a map, or the literal false from the server) or false when the request failed.
     * The result may be null when the key is missing. `select` may be null (e.g. 'nama, nomorHP, alamat').
     */
    public static JsonElement getDetailActiveUser(String select) {
        String selectQS = (select != null) ? "getUserEntities=" + select : "";

        String params = HTTPRequest.getQueryString(selectQS, true);

        Http.Response httpResponse = Http.getApi("/User/getDetailUser?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return dataObject.get("detailUser");
        }

        return new JsonPrimitive(false);
    }

    /** Dart: getDetailActiveUser() without select. */
    public static JsonElement getDetailActiveUser() {
        return getDetailActiveUser(null);
    }

    /** POST /User/updateProfil with the changed fields -> statusUpdateProfil. */
    public static boolean simpanPerubahanDataProfil(JsonObject dataPerubahan) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (dataPerubahan != null) {
            for (Map.Entry<String, JsonElement> e : dataPerubahan.entrySet()) {
                fields.put(e.getKey(), Json.asStr(e.getValue()));
            }
        }
        return simpanPerubahanDataProfil(fields);
    }

    /** Same as above for callers that already hold the fields as a String map. */
    public static boolean simpanPerubahanDataProfil(Map<String, String> dataPerubahan) {
        Map<String, String> body = HTTPRequest.postBody(dataPerubahan);
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.postApi("/User/updateProfil?" + params, body);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusUpdateProfil");
        }

        return false;
    }
}
