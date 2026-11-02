package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.util.Json;
import id.laukpauk.data.local.LocalStorage;

/** Port of lib/models/ROModel.dart (repeat order). Blocking (network): call from a background thread. */
public final class ROModel {
    private final HTTPRequest httpRequest;
    private final Http http;
    private final LocalStorage localStorage;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public ROModel(HTTPRequest httpRequest, Http http, LocalStorage localStorage) {
        this.httpRequest = httpRequest;
        this.http = http;
        this.localStorage = localStorage;
    }

    /**
     * POST /RO/addToCartThesePenjual. `listPenjual` is joined with ', ' exactly like Dart's
     * listPenjual.join(', '). Returns the server answer, or {"statusAddToCart": false}.
     */
    public JsonObject addToCartThesePenjual(String idTransaksi, List<String> listPenjual) {
        String cartCode = localStorage.getCartCode();
        String selectedSellersString = (listPenjual == null) ? "" : String.join(", ", listPenjual);

        Map<String, String> httpBody = new LinkedHashMap<>();
        httpBody.put("idTransaksi", idTransaksi);
        httpBody.put("cartCode", cartCode);
        httpBody.put("selectedSellers", selectedSellersString);

        String params = httpRequest.getQueryString(null, true);
        Map<String, String> body = httpRequest.postBody(httpBody);

        Http.Response httpResponse = http.postApi("/RO/addToCartThesePenjual?" + params, body);
        if (httpResponse.statusCode == 200) {
            return httpResponse.jsonObject();
        }

        return Json.of("statusAddToCart", false);
    }

    /** Same as above for callers that hold the seller ids in a JsonArray. */
    public JsonObject addToCartThesePenjual(String idTransaksi, JsonArray listPenjual) {
        java.util.ArrayList<String> sellers = new java.util.ArrayList<>();
        if (listPenjual != null) {
            for (JsonElement e : listPenjual) {
                sellers.add(Json.asStr(e));
            }
        }
        return addToCartThesePenjual(idTransaksi, sellers);
    }

    /** GET /RO/getItemTransaksi -> the "itemTransaksi" list ([] on failure). */
    public JsonArray getItemTransaksiPerPenjual(String idTransaksi) {
        JsonObject savedADJD = localStorage.getSavedLatitudeAndLongitude();

        String latDelivery = Json.str(savedADJD, "latitude", "");
        String longDelivery = Json.str(savedADJD, "longitude", "");
        String jadwalDeliveryCode = Json.str(savedADJD, "jadwalDeliveryCode", "");

        String params = httpRequest.getQueryString(
                "idTransaksi=" + idTransaksi
                        + "&latDelivery=" + latDelivery
                        + "&longDelivery=" + longDelivery
                        + "&jadwalDeliveryCode=" + jadwalDeliveryCode,
                true);

        String stringURL = "/RO/getItemTransaksi?" + params;

        Http.Response httpResponse = http.getApi(stringURL);

        if (httpResponse.statusCode == 200) {
            return Json.array(httpResponse.jsonObject(), "itemTransaksi");
        }

        return new JsonArray();
    }
}
