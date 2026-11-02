package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.Map;

import id.laukpauk.core.AuthModel;
import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/NotifikasiModel.dart. Blocking (network): call from a background thread. */
public final class NotifikasiModel {
    private final AuthModel authModel;
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public NotifikasiModel(AuthModel authModel, HTTPRequest httpRequest, Http http) {
        this.authModel = authModel;
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /** GET /User/getNotifikasi -> "notifikasi" ([] on failure). */
    public JsonArray getUpdateNotifikasi() {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/User/getNotifikasi?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "notifikasi");
        }

        return new JsonArray();
    }

    /** GET /User/getNotifikasiKhusus[/idUser] -> "notifikasi" ([] on failure). `idUser` may be null. */
    public JsonArray getNotifikasiKhusus(String idUser) {
        String params = httpRequest.getQueryString(null, true);
        String userID = (idUser == null) ? "" : "/" + idUser;

        Http.Response httpResponse = http.getApi("/User/getNotifikasiKhusus" + userID + "?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "notifikasi");
        }

        return new JsonArray();
    }

    /** Dart: getNotifikasiKhusus() without idUser. */
    public JsonArray getNotifikasiKhusus() {
        return getNotifikasiKhusus(null);
    }

    /** GET /User/getJumlahNotifikasiKhusus?status=.. (0 when not logged in / on failure). */
    public int getJumlahNotifikasiKhusus(String statusNotifikasi) {
        boolean isLogin = authModel.isLogin();
        if (isLogin) {
            String params = httpRequest.getQueryString("status=" + statusNotifikasi, true);

            Http.Response httpResponse = http.getApi("/User/getJumlahNotifikasiKhusus?" + params);

            if (httpResponse.statusCode == 200) {
                return Json.intVal(httpResponse.jsonObject(), "jumlahNotifikasi");
            }
        }
        return 0;
    }

    /** GET /User/getJumlahNotifikasi?status=.. (0 when not logged in / on failure). */
    public int getJumlahNotifikasi(String statusNotifikasi) {
        boolean isLogin = authModel.isLogin();
        if (isLogin) {
            String params = httpRequest.getQueryString("status=" + statusNotifikasi, true);

            Http.Response httpResponse = http.getApi("/User/getJumlahNotifikasi?" + params);

            if (httpResponse.statusCode == 200) {
                return Json.intVal(httpResponse.jsonObject(), "jumlahNotifikasi");
            }
        }
        return 0;
    }

    /**
     * GET /Notifikasi/getNotifikasi/{id}. Dart returned `dynamic`: the "notifikasi" value
     * (a map, or the literal false from the server) or false when the request failed.
     * The result may be null when the key is missing.
     */
    public JsonElement getDetailNotifikasiGlobal(String idNotifikasi) {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/Notifikasi/getNotifikasi/" + idNotifikasi + "?" + params);

        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject().get("notifikasi");
        }
        return new JsonPrimitive(false);
    }

    /** POST /Notifikasi/setAs (idNotifikasiItem, setAs, jenisNotifikasi) -> statusSetAs. */
    public boolean setAs(String idNotifikasiItem, String setAsParam, String jenisNotifikasi) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("idNotifikasiItem", idNotifikasiItem);
        body.put("setAs", setAsParam);
        body.put("jenisNotifikasi", jenisNotifikasi);

        String params = httpRequest.getQueryString(null, true);
        Map<String, String> httpBody = httpRequest.postBody(body);

        Http.Response httpResponse = http.postApi("/Notifikasi/setAs?" + params, httpBody);
        if (httpResponse.statusCode == 200) {
            JsonObject serverResponse = httpResponse.jsonObject();
            return Json.bool(serverResponse, "statusSetAs");
        }
        return false;
    }
}
