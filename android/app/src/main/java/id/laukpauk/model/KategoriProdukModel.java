package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/KategoriProdukModel.dart. Every method is BLOCKING (network). */
public final class KategoriProdukModel {
    private KategoriProdukModel() {}

    /** Dart: getKategoriProduk({int? limitData}) -> Future&lt;List&gt;. `limitData` may be null. */
    public static JsonArray getKategoriProduk(Integer limitData) {
        String limitDataQS = (limitData != null)
                ? "limitKategoriProduk=" + limitData
                : "";

        String params = HTTPRequest.getQueryString(limitDataQS);

        Http.Response httpResponse = Http.getApi("/KategoriProduk/getKategoriProduk?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "kategoriProduk");
        }
        return new JsonArray();
    }

    public static JsonArray getKategoriProduk() {
        return getKategoriProduk(null);
    }

    /** Dart: getProdukInThisCategory(idKategoriProduk, {String? select}) -> Future&lt;List&gt;. `select` may be null. */
    public static JsonArray getProdukInThisCategory(String idKategoriProduk, String select) {
        String selectQS = (select != null) ? "select=" + select : "";
        String params = HTTPRequest.getQueryString(selectQS);

        Http.Response httpResponse = Http.getApi(
                "/Produk/getProductsInThisCategory/" + idKategoriProduk + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject returnedData = httpResponse.jsonObject();
            return Json.array(returnedData, "productInThisCategory");
        }
        return new JsonArray();
    }

    public static JsonArray getProdukInThisCategory(String idKategoriProduk) {
        return getProdukInThisCategory(idKategoriProduk, null);
    }
}
