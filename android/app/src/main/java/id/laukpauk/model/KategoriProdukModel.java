package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/KategoriProdukModel.dart. Every method is BLOCKING (network). */
public final class KategoriProdukModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public KategoriProdukModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /** Dart: getKategoriProduk({int? limitData}) -> Future&lt;List&gt;. `limitData` may be null. */
    public JsonArray getKategoriProduk(Integer limitData) {
        String limitDataQS = (limitData != null)
                ? "limitKategoriProduk=" + limitData
                : "";

        String params = httpRequest.getQueryString(limitDataQS);

        Http.Response httpResponse = http.getApi("/KategoriProduk/getKategoriProduk?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "kategoriProduk");
        }
        return new JsonArray();
    }

    public JsonArray getKategoriProduk() {
        return getKategoriProduk(null);
    }

    /** Dart: getProdukInThisCategory(idKategoriProduk, {String? select}) -> Future&lt;List&gt;. `select` may be null. */
    public JsonArray getProdukInThisCategory(String idKategoriProduk, String select) {
        String selectQS = (select != null) ? "select=" + select : "";
        String params = httpRequest.getQueryString(selectQS);

        Http.Response httpResponse = http.getApi(
                "/Produk/getProductsInThisCategory/" + idKategoriProduk + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject returnedData = httpResponse.jsonObject();
            return Json.array(returnedData, "productInThisCategory");
        }
        return new JsonArray();
    }

    public JsonArray getProdukInThisCategory(String idKategoriProduk) {
        return getProdukInThisCategory(idKategoriProduk, null);
    }
}
