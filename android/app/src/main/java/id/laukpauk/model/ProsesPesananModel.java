package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/ProsesPesananModel.dart. Every method is BLOCKING (network). */
public final class ProsesPesananModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public ProsesPesananModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /** Dart: prosesPesanan(idTransaksi) -> Future&lt;bool&gt; (a POST without a body). */
    public boolean prosesPesanan(String idTransaksi) {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.postApi(
                "/Transaksi/prosesPesanan/" + idTransaksi + "?" + params, new HashMap<String, String>());
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusProsesPesanan");
        }
        return false;
    }

    /** Dart: getPesananMasukItem(idTransaksi) -> Future&lt;List&gt;. */
    public JsonArray getPesananMasukItem(String idTransaksi) {
        String pesananEntity =
                "id, quantity, namaProduk, namaSatuan, harga, diskon, fotoProduk, catatan, jenisProduk, status";
        String params = httpRequest.getQueryString("pesananEntity=" + pesananEntity, true);

        Http.Response httpResponse = http.getApi(
                "/Penjual/getPesananMasukItem/" + idTransaksi + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "pesananMasukItem");
        }

        return new JsonArray();
    }
}
