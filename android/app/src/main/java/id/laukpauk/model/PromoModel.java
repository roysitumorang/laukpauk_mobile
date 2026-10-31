package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;

/** Port of lib/models/PromoModel.dart. Every method is BLOCKING (network). */
public final class PromoModel {
    private PromoModel() {}

    /** Dart: getProdukPromo() -> Future&lt;List&gt;. Empty when no address / schedule is set. */
    public static JsonArray getProdukPromo() {
        boolean isADJDSet = LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
        if (isADJDSet) {
            JsonObject adjdSet = LocalStorage.getSavedLatitudeAndLongitude();
            String latitude = Json.strOrNull(adjdSet, "latitude");
            String longitude = Json.strOrNull(adjdSet, "longitude");
            String jadwalDeliveryCode = Json.strOrNull(adjdSet, "jadwalDeliveryCode");
            if (latitude == null || longitude == null || jadwalDeliveryCode == null) {
                return new JsonArray(); // Dart: assigning null to a String throws
            }

            String params = HTTPRequest.getQueryString(
                    "latitude=" + latitude + "&longitude=" + longitude
                            + "&jadwalDeliveryCode=" + jadwalDeliveryCode,
                    true);

            Http.Response httpResponse = Http.getApi("/Promo/getPromoAroundOfThisLatLong?" + params);

            if (httpResponse.statusCode == 200) {
                JsonObject dataObject = httpResponse.jsonObject();
                return Json.array(dataObject, "produkPromo");
            }
        }
        return new JsonArray();
    }

    /** Dart: getDetailPromoItem(idPromoItem) -> Future&lt;dynamic&gt;. `promoItem`, or false on failure. */
    public static JsonElement getDetailPromoItem(String idPromoItem) {
        Http.Response httpResponse = Http.getApi("/Promo/getPromoItem/" + idPromoItem);

        if (httpResponse.statusCode == 200) {
            JsonObject serverResponse = httpResponse.jsonObject();
            return ShopModelUi.dyn(serverResponse.get("promoItem"));
        }
        return ShopModelUi.FALSE;
    }
}
