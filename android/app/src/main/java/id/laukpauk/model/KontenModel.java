package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/KontenModel.dart. Every method is BLOCKING (network). */
public final class KontenModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public KontenModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /** Dart: getProdukKonten(idKonten, {int? limit}) -> Future&lt;List&gt;. `limit` may be null. */
    public JsonArray getProdukKonten(String idKonten, Integer limit) {
        String limitQS = (limit == null) ? "" : "limit=" + limit;

        String params = httpRequest.getQueryString(limitQS);

        Http.Response httpResponse = http.getApi(
                "/Konten/getKontenItemFromThisKonten/" + idKonten + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "kontenItem");
        }

        return new JsonArray();
    }

    public JsonArray getProdukKonten(String idKonten) {
        return getProdukKonten(idKonten, null);
    }

    /** Dart: getKontenAplikasi({String page = 'homepage'}) -> Future&lt;List&gt;. */
    public JsonArray getKontenAplikasi(String page) {
        String params = httpRequest.getQueryString("page=" + page);

        Http.Response httpResponse = http.getApi("/Konten/getKonten?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "dataKonten");
        }

        return new JsonArray();
    }

    public JsonArray getKontenAplikasi() {
        return getKontenAplikasi("homepage");
    }
}
