package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.util.Json;
import id.laukpauk.util.ServerPath;

/** Port of lib/models/HomepageModel.dart. Every method is BLOCKING (network). */
public final class HomepageModel {
    private final HTTPRequest httpRequest;
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public HomepageModel(HTTPRequest httpRequest, Http http) {
        this.httpRequest = httpRequest;
        this.http = http;
    }

    /** Dart: getArtikel() -> Future&lt;List&gt;. */
    public JsonArray getArtikel() {
        Http.Response httpResponse = http.getApi("/Artikel/getArtikel");

        if (httpResponse.statusCode == 200) {
            JsonObject dataArtikelObject = httpResponse.jsonObject();
            return Json.array(dataArtikelObject, "listArtikel");
        }
        return new JsonArray();
    }

    /** Dart: getJumlahVoucher() -> Future&lt;int&gt;. */
    public int getJumlahVoucher() {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/User/getJumlahVoucher?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();

            return Json.intVal(dataObject, "jumlahVoucher");
        }

        return 0;
    }

    /** Dart: getTotalDompet() -> Future&lt;int&gt;. */
    public int getTotalDompet() {
        String params = httpRequest.getQueryString(null, true);

        Http.Response httpResponse = http.getApi("/User/getTotalDompet?" + params);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.intVal(dataObject, "totalDompet");
        }

        return 0;
    }

    /**
     * Dart: getDataPasar({required isADJDSet, showAll = false, limitPasar = 2, pencarian, savedADJD})
     * -> Future&lt;List&gt;. `pencarian` and `savedADJD` may be null. The saved ADJD map has the
     * keys latitude, longitude, jadwalDeliveryCode (see LocalStorage.getSavedLatitudeAndLongitude).
     */
    public JsonArray getDataPasar(boolean isADJDSet, boolean showAll, int limitPasar,
                                         String pencarian, JsonObject savedADJD) {
        String limitPasarQS = showAll ? "" : "limitPasar=" + limitPasar;
        String namaPasarQS = "";

        if (pencarian != null) {
            if (pencarian.length() >= 1) {
                namaPasarQS = "namaPasar=" + Http.enc(pencarian);
            }
        }

        String url = ServerPath.apiBaseURL + "/Pasar/getPasar?" + limitPasarQS + "&" + namaPasarQS;

        String keyOfData = "dataPasar";

        if (isADJDSet) {
            String adjdLatitude = null;
            String adjdLongitude = null;
            String adjdKodeJadwalDelivery = null;

            if (savedADJD != null) {
                adjdLatitude = Json.strOrNull(savedADJD, "latitude");
                adjdLongitude = Json.strOrNull(savedADJD, "longitude");
                adjdKodeJadwalDelivery = Json.strOrNull(savedADJD, "jadwalDeliveryCode");
            }

            if (adjdLatitude != null && adjdLongitude != null && adjdKodeJadwalDelivery != null) {
                url = ServerPath.apiBaseURL
                        + "/Pasar/getPasarAroundOfThisLatLong?latitude=" + adjdLatitude
                        + "&longitude=" + adjdLongitude
                        + "&jadwalDeliveryCode=" + adjdKodeJadwalDelivery
                        + "&" + limitPasarQS + "&" + namaPasarQS;
                keyOfData = "listPasar";
            }
        }

        Http.Response httpResponse = http.get(url);

        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();
            return Json.array(dataObject, keyOfData);
        }

        return new JsonArray();
    }
}
