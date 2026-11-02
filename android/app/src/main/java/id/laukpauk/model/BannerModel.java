package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import id.laukpauk.core.Http;
import id.laukpauk.util.Json;

/** Port of lib/models/BannerModel.dart. */
public final class BannerModel {
    private final Http http;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public BannerModel(Http http) {
        this.http = http;
    }

    /** Dart: getBanner() -> Future&lt;List&gt;. BLOCKING. */
    public JsonArray getBanner() {
        Http.Response httpResponse = http.getApi("/Banner/getBanner");

        if (httpResponse.statusCode == 200) {
            JsonObject dataBannerObject = httpResponse.jsonObject();
            return Json.array(dataBannerObject, "listBanner");
        }
        return new JsonArray();
    }
}
