package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/HistoriPenjualanModel.dart. Every method is BLOCKING (network). */
public final class HistoriPenjualanModel {
    private HistoriPenjualanModel() {}

    /**
     * Dart: getHistoriPenjualan({String? idPenjual, String? select, String? statusTransaksi})
     * -> Future&lt;List&gt;. Every argument may be null.
     */
    public static JsonArray getHistoriPenjualan(String idPenjual, String select,
                                                String statusTransaksi) {
        String idPenjualPath = (idPenjual == null) ? "" : "/" + idPenjual;

        String selectTransaksi = (select == null) ? "" : "selectTransaksi=" + select;
        String statusTransaksiQS = (statusTransaksi == null) ? "" : "&statusTransaksi=" + statusTransaksi;

        String params = HTTPRequest.getQueryString(selectTransaksi + statusTransaksiQS, true);
        Http.Response httpResponse = Http.getApi(
                "/Penjual/getHistoriPenjualan" + idPenjualPath + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "historiPenjualan");
        }

        return new JsonArray();
    }

    public static JsonArray getHistoriPenjualan() {
        return getHistoriPenjualan(null, null, null);
    }

    /** Dart: getTotalTransaksiForThisPenjual(idTransaksi, {String? idPenjual}) -> Future&lt;int&gt;. `idPenjual` may be null. */
    public static int getTotalTransaksiForThisPenjual(String idTransaksi, String idPenjual) {
        String params = HTTPRequest.getQueryString(null, true);
        String idPenjualPath = (idPenjual == null) ? "" : "/" + idPenjual;

        Http.Response httpResponse = Http.getApi(
                "/Transaksi/getTotalTransaksiForThisPenjual/" + idTransaksi + idPenjualPath + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "totalTransaksi");
        }

        return 0;
    }

    public static int getTotalTransaksiForThisPenjual(String idTransaksi) {
        return getTotalTransaksiForThisPenjual(idTransaksi, null);
    }
}
