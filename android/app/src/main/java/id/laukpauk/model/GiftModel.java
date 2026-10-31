package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.element.Gift;

/** Port of lib/models/GiftModel.dart. Both methods are BLOCKING (network). */
public final class GiftModel {
    private GiftModel() {}

    public static List<Gift> getGifts() {
        String params = HTTPRequest.getQueryString();

        Http.Response httpResponse = Http.getApi("/Gift/getGift?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();

            JsonArray rawGift = Json.array(dataObject, "gift");
            List<Gift> gift = new ArrayList<>();
            for (JsonElement giftItem : rawGift) {
                gift.add(Gift.fromJson(Json.obj(giftItem)));
            }

            return gift;
        }

        return new ArrayList<>();
    }

    /** null when the gift does not exist (the API answers `false`) or the request failed. */
    public static Gift getGift(String idGift) {
        String params = HTTPRequest.getQueryString();

        Http.Response httpResponse = Http.getApi("/Gift/getGift/" + idGift + "?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();

            JsonElement rawGift = Json.get(dataObject, "gift");
            if (!Json.isFalse(rawGift) && Json.isObject(rawGift)) {
                return Gift.fromJson(rawGift.getAsJsonObject());
            }
        }

        return null;
    }
}
