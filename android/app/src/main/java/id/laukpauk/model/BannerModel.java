package id.laukpauk.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Http;
import id.laukpauk.core.Json;

/** Port of lib/models/BannerModel.dart. */
public final class BannerModel {
    private BannerModel() {}

    /** Dart: getBanner() -> Future&lt;List&gt;. BLOCKING. */
    public static JsonArray getBanner() {
        Http.Response httpResponse = Http.getApi("/Banner/getBanner");

        if (httpResponse.statusCode == 200) {
            JsonObject dataBannerObject = httpResponse.jsonObject();
            return Json.array(dataBannerObject, "listBanner");
        }
        return new JsonArray();
    }
}
