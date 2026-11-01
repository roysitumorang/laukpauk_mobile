package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/ProsesPesananModel.dart. Every method is BLOCKING (network). */
public final class ProsesPesananModel {
    private ProsesPesananModel() {}

    /** Dart: prosesPesanan(idTransaksi) -> Future&lt;bool&gt; (a POST without a body). */
    public static boolean prosesPesanan(String idTransaksi) {
        String params = HTTPRequest.getQueryString(null, true);

        Http.Response httpResponse = Http.postApi(
                "/Transaksi/prosesPesanan/" + idTransaksi + "?" + params, new HashMap<String, String>());
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.bool(dataObject, "statusProsesPesanan");
        }
        return false;
    }

    /** Dart: getPesananMasukItem(idTransaksi) -> Future&lt;List&gt;. */
    public static JsonArray getPesananMasukItem(String idTransaksi) {
        String pesananEntity =
                "id, quantity, namaProduk, namaSatuan, harga, diskon, fotoProduk, catatan, jenisProduk, status";
        String params = HTTPRequest.getQueryString("pesananEntity=" + pesananEntity, true);

        Http.Response httpResponse = Http.getApi(
                "/Penjual/getPesananMasukItem/" + idTransaksi + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "pesananMasukItem");
        }

        return new JsonArray();
    }
}
