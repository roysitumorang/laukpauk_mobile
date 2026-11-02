package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.util.Json;

/** Port of lib/models/ProfilSayaModel.dart. Blocking (network): call from a background thread. */
public final class ProfilSayaModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public ProfilSayaModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /**
     * GET /User/getDetailUser[?getUserEntities=select]. Dart returned `dynamic`: the "detailUser" value
     * (a map, or the literal false from the server) or false when the request failed.
     * The result may be null when the key is missing. `select` may be null (e.g. 'nama, nomorHP, alamat').
     */
    public JsonElement getDetailActiveUser(String select) {
        String selectQS = (select != null) ? "getUserEntities=" + select : "";

        String params = httpRequest.getQueryString(selectQS, true);

        Http.Response httpResponse = http.getApi("/User/getDetailUser?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return dataObject.get("detailUser");
        }

        return new JsonPrimitive(false);
    }

    /** Dart: getDetailActiveUser() without select. */
    public JsonElement getDetailActiveUser() {
        return getDetailActiveUser(null);
    }

    /** POST /User/updateProfil with the changed fields -> statusUpdateProfil. */
    public boolean simpanPerubahanDataProfil(JsonObject dataPerubahan) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (dataPerubahan != null) {
            for (Map.Entry<String, JsonElement> e : dataPerubahan.entrySet()) {
                fields.put(e.getKey(), Json.asStr(e.getValue()));
            }
        }
        return simpanPerubahanDataProfil(fields);
    }

    /** Same as above for callers that already hold the fields as a String map. */
    public boolean simpanPerubahanDataProfil(Map<String, String> dataPerubahan) {
        Map<String, String> body = httpRequest.postBody(dataPerubahan);
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.postApi("/User/updateProfil?" + params, body);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusUpdateProfil");
        }

        return false;
    }
}
