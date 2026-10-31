package id.laukpauk.model;

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
    private NotifikasiModel() {}

    /** GET /User/getNotifikasi -> "notifikasi" ([] on failure). */
    public static JsonArray getUpdateNotifikasi() {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/User/getNotifikasi?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "notifikasi");
        }

        return new JsonArray();
    }

    /** GET /User/getNotifikasiKhusus[/idUser] -> "notifikasi" ([] on failure). `idUser` may be null. */
    public static JsonArray getNotifikasiKhusus(String idUser) {
        String params = HTTPRequest.getQueryString(null, true);
        String userID = (idUser == null) ? "" : "/" + idUser;

        Http.Response httpResponse = Http.getApi("/User/getNotifikasiKhusus" + userID + "?" + params);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "notifikasi");
        }

        return new JsonArray();
    }

    /** Dart: getNotifikasiKhusus() without idUser. */
    public static JsonArray getNotifikasiKhusus() {
        return getNotifikasiKhusus(null);
    }

    /** GET /User/getJumlahNotifikasiKhusus?status=.. (0 when not logged in / on failure). */
    public static int getJumlahNotifikasiKhusus(String statusNotifikasi) {
        boolean isLogin = AuthModel.isLogin();
        if (isLogin) {
            String params = HTTPRequest.getQueryString("status=" + statusNotifikasi, true);

            Http.Response httpResponse = Http.getApi("/User/getJumlahNotifikasiKhusus?" + params);

            if (httpResponse.statusCode == 200) {
                return Json.intVal(httpResponse.jsonObject(), "jumlahNotifikasi");
            }
        }
        return 0;
    }

    /** GET /User/getJumlahNotifikasi?status=.. (0 when not logged in / on failure). */
    public static int getJumlahNotifikasi(String statusNotifikasi) {
        boolean isLogin = AuthModel.isLogin();
        if (isLogin) {
            String params = HTTPRequest.getQueryString("status=" + statusNotifikasi, true);

            Http.Response httpResponse = Http.getApi("/User/getJumlahNotifikasi?" + params);

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
    public static JsonElement getDetailNotifikasiGlobal(String idNotifikasi) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.getApi("/Notifikasi/getNotifikasi/" + idNotifikasi + "?" + params);

        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject().get("notifikasi");
        }
        return new JsonPrimitive(false);
    }

    /** POST /Notifikasi/setAs (idNotifikasiItem, setAs, jenisNotifikasi) -> statusSetAs. */
    public static boolean setAs(String idNotifikasiItem, String setAsParam, String jenisNotifikasi) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("idNotifikasiItem", idNotifikasiItem);
        body.put("setAs", setAsParam);
        body.put("jenisNotifikasi", jenisNotifikasi);

        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> httpBody = HTTPRequest.postBody(body);

        Http.Response httpResponse = Http.postApi("/Notifikasi/setAs?" + params, httpBody);
        if (httpResponse.statusCode == 200) {
            JsonObject serverResponse = httpResponse.jsonObject();
            return Json.bool(serverResponse, "statusSetAs");
        }
        return false;
    }
}
