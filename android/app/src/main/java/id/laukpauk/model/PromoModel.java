package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;

/** Port of lib/models/PromoModel.dart. Every method is BLOCKING (network). */
public final class PromoModel {
    private final HTTPRequest httpRequest;
    private final Http http;
    private final LocalStorage localStorage;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public PromoModel(HTTPRequest httpRequest, Http http, LocalStorage localStorage) {
        this.httpRequest = httpRequest;
        this.http = http;
        this.localStorage = localStorage;
    }

    /** Dart: getProdukPromo() -> Future&lt;List&gt;. Empty when no address / schedule is set. */
    public JsonArray getProdukPromo() {
        boolean isADJDSet = localStorage.isAlamatDeliveryDanJadwalDeliverySet();
        if (isADJDSet) {
            JsonObject adjdSet = localStorage.getSavedLatitudeAndLongitude();
            String latitude = Json.strOrNull(adjdSet, "latitude");
            String longitude = Json.strOrNull(adjdSet, "longitude");
            String jadwalDeliveryCode = Json.strOrNull(adjdSet, "jadwalDeliveryCode");
            if (latitude == null || longitude == null || jadwalDeliveryCode == null) {
                return new JsonArray(); // Dart: assigning null to a String throws
            }

            String params = httpRequest.getQueryString(
                    "latitude=" + latitude + "&longitude=" + longitude
                            + "&jadwalDeliveryCode=" + jadwalDeliveryCode,
                    true);

            Http.Response httpResponse = http.getApi("/Promo/getPromoAroundOfThisLatLong?" + params);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.array(dataObject, "produkPromo");
            }
        }
        return new JsonArray();
    }

    /** Dart: getDetailPromoItem(idPromoItem) -> Future&lt;dynamic&gt;. `promoItem`, or false on failure. */
    public JsonElement getDetailPromoItem(String idPromoItem) {
        Http.Response httpResponse = http.getApi("/Promo/getPromoItem/" + idPromoItem);

        if (httpResponse.statusCode == 200) {
            JsonObject serverResponse = httpResponse.jsonObject();
            return ShopModelUi.dyn(serverResponse.get("promoItem"));
        }
        return ShopModelUi.FALSE;
    }
}
