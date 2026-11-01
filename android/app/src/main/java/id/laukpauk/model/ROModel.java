package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;

/** Port of lib/models/ROModel.dart (repeat order). Blocking (network): call from a background thread. */
public final class ROModel {
    private ROModel() {}

    /**
     * POST /RO/addToCartThesePenjual. `listPenjual` is joined with ', ' exactly like Dart's
     * listPenjual.join(', '). Returns the server answer, or {"statusAddToCart": false}.
     */
    public static JsonObject addToCartThesePenjual(String idTransaksi, List<String> listPenjual) {
        String cartCode = LocalStorage.getCartCode();
        String selectedSellersString = (listPenjual == null) ? "" : String.join(", ", listPenjual);

        Map<String, String> httpBody = new LinkedHashMap<>();
        httpBody.put("idTransaksi", idTransaksi);
        httpBody.put("cartCode", cartCode);
        httpBody.put("selectedSellers", selectedSellersString);

        String params = HTTPRequest.getQueryString(null, true);
        Map<String, String> body = HTTPRequest.postBody(httpBody);

        Http.Response httpResponse = Http.postApi("/RO/addToCartThesePenjual?" + params, body);
        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject();
        }

        return Json.of("statusAddToCart", false);
    }

    /** Same as above for callers that hold the seller ids in a JsonArray. */
    public static JsonObject addToCartThesePenjual(String idTransaksi, JsonArray listPenjual) {
        java.util.ArrayList<String> sellers = new java.util.ArrayList<>();
        if (listPenjual != null) {
            for (JsonElement e : listPenjual) {
                sellers.add(Json.asStr(e));
            }
        }
        return addToCartThesePenjual(idTransaksi, sellers);
    }

    /** GET /RO/getItemTransaksi -> the "itemTransaksi" list ([] on failure). */
    public static JsonArray getItemTransaksiPerPenjual(String idTransaksi) {
        JsonObject savedADJD = LocalStorage.getSavedLatitudeAndLongitude();

        String latDelivery = Json.str(savedADJD, "latitude", "");
        String longDelivery = Json.str(savedADJD, "longitude", "");
        String jadwalDeliveryCode = Json.str(savedADJD, "jadwalDeliveryCode", "");

        String params = HTTPRequest.getQueryString(
                "idTransaksi=" + idTransaksi
                        + "&latDelivery=" + latDelivery
                        + "&longDelivery=" + longDelivery
                        + "&jadwalDeliveryCode=" + jadwalDeliveryCode,
                true);

        String stringURL = "/RO/getItemTransaksi?" + params;

        Http.Response httpResponse = Http.getApi(stringURL);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "itemTransaksi");
        }

        return new JsonArray();
    }
}
