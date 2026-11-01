package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/KontenModel.dart. Every method is BLOCKING (network). */
public final class KontenModel {
    private KontenModel() {}

    /** Dart: getProdukKonten(idKonten, {int? limit}) -> Future&lt;List&gt;. `limit` may be null. */
    public static JsonArray getProdukKonten(String idKonten, Integer limit) {
        String limitQS = (limit == null) ? "" : "limit=" + limit;

        String params = HTTPRequest.getQueryString(limitQS);

        Http.Response httpResponse = Http.getApi(
                "/Konten/getKontenItemFromThisKonten/" + idKonten + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "kontenItem");
        }

        return new JsonArray();
    }

    public static JsonArray getProdukKonten(String idKonten) {
        return getProdukKonten(idKonten, null);
    }

    /** Dart: getKontenAplikasi({String page = 'homepage'}) -> Future&lt;List&gt;. */
    public static JsonArray getKontenAplikasi(String page) {
        String params = HTTPRequest.getQueryString("page=" + page);

        Http.Response httpResponse = Http.getApi("/Konten/getKonten?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "dataKonten");
        }

        return new JsonArray();
    }

    public static JsonArray getKontenAplikasi() {
        return getKontenAplikasi("homepage");
    }
}
