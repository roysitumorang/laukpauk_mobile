package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.util.Json;
import id.laukpauk.data.local.LocalStorage;

/** Port of lib/models/PasarModel.dart. BLOCKING (network). */
public final class PasarModel {
    private final HTTPRequest httpRequest;
    private final Http http;
    private final LocalStorage localStorage;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public PasarModel(HTTPRequest httpRequest, Http http, LocalStorage localStorage) {
        this.httpRequest = httpRequest;
        this.http = http;
        this.localStorage = localStorage;
    }

    /**
     * Dart: getProdukInThisPasar(idPasar, {required useLimit, startFrom, cariProduk}) -> Future&lt;List&gt;.
     * `startFrom` and `cariProduk` may be null.
     */
    public JsonArray getProdukInThisPasar(String idPasar, String useLimit, String startFrom,
                                                 String cariProduk) {
        // auth token dipakai untuk mencegah mendapatkan produk user yang sedang login jika dia seorang penjual
        // hal ini bertujuan supaya user tsb tidak bisa order produknya sendiri

        boolean isADJDSet = localStorage.isAlamatDeliveryDanJadwalDeliverySet();
        String jadwalDeliveryCodeQS = "";
        if (isADJDSet) {
            JsonObject adjdSet = localStorage.getSavedLatitudeAndLongitude();
            String jadwalDeliveryCode = Json.strOrNull(adjdSet, "jadwalDeliveryCode");
            if (jadwalDeliveryCode == null) {
                return new JsonArray(); // Dart: 'jadwalDeliveryCode=' + null throws
            }
            jadwalDeliveryCodeQS = "jadwalDeliveryCode=" + jadwalDeliveryCode;
        }

        String startFromQS = "";
        if (startFrom != null) {
            startFromQS = "&startFrom=" + startFrom;
        }

        String useLimitQS = "&useLimit=" + useLimit;
        String cariProdukQS = "";
        if (cariProduk != null) {
            cariProdukQS = "&cariProduk=" + Http.enc(cariProduk);
        }

        String params = httpRequest.getQueryString(
                jadwalDeliveryCodeQS + startFromQS + useLimitQS + cariProdukQS, true);
        Http.Response httpResponse = http.getApi("/Pasar/getProdukInThisPasar/" + idPasar + "?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, "listProduk");
        }

        return new JsonArray();
    }
}
