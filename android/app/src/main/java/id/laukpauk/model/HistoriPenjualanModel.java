package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/HistoriPenjualanModel.dart. Every method is BLOCKING (network). */
public final class HistoriPenjualanModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public HistoriPenjualanModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /**
     * Dart: getHistoriPenjualan({String? idPenjual, String? select, String? statusTransaksi})
     * -> Future&lt;List&gt;. Every argument may be null.
     */
    public JsonArray getHistoriPenjualan(String idPenjual, String select,
                                                String statusTransaksi) {
        String idPenjualPath = (idPenjual == null) ? "" : "/" + idPenjual;

        String selectTransaksi = (select == null) ? "" : "selectTransaksi=" + select;
        String statusTransaksiQS = (statusTransaksi == null) ? "" : "&statusTransaksi=" + statusTransaksi;

        String params = httpRequest.getQueryString(selectTransaksi + statusTransaksiQS, true);
        Http.Response httpResponse = http.getApi(
                "/Penjual/getHistoriPenjualan" + idPenjualPath + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "historiPenjualan");
        }

        return new JsonArray();
    }

    public JsonArray getHistoriPenjualan() {
        return getHistoriPenjualan(null, null, null);
    }

    /** Dart: getTotalTransaksiForThisPenjual(idTransaksi, {String? idPenjual}) -> Future&lt;int&gt;. `idPenjual` may be null. */
    public int getTotalTransaksiForThisPenjual(String idTransaksi, String idPenjual) {
        String params = httpRequest.getQueryString(null, true);
        String idPenjualPath = (idPenjual == null) ? "" : "/" + idPenjual;

        Http.Response httpResponse = http.getApi(
                "/Transaksi/getTotalTransaksiForThisPenjual/" + idTransaksi + idPenjualPath + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "totalTransaksi");
        }

        return 0;
    }

    public int getTotalTransaksiForThisPenjual(String idTransaksi) {
        return getTotalTransaksiForThisPenjual(idTransaksi, null);
    }
}
