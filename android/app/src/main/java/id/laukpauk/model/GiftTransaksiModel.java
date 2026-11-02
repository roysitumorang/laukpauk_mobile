package id.laukpauk.model;

import id.laukpauk.core.AppContainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.core.HTTPRequest;
import id.laukpauk.core.Http;
import id.laukpauk.core.Json;
import id.laukpauk.core.LocalStorage;
import id.laukpauk.element.GiftTransaksi;

/** Port of lib/models/GiftTransaksiModel.dart. BLOCKING (network). */
public final class GiftTransaksiModel {
    private final HTTPRequest httpRequest;
    private final Http http;
    private final LocalStorage localStorage;

    /** Created once by {@link AppContainer}; dependencies are injected, never looked up. */
    public GiftTransaksiModel(HTTPRequest httpRequest, Http http, LocalStorage localStorage) {
        this.httpRequest = httpRequest;
        this.http = http;
        this.localStorage = localStorage;
    }

    /** Dart: getGiftTransaksi({String? cartCode}); pass null for the stored cart code. */
    public List<GiftTransaksi> getGiftTransaksi(String cartCode) {
        List<GiftTransaksi> giftTransaksi = new ArrayList<>();

        String usedCartCode = localStorage.getCartCode();
        if (cartCode != null) {
            usedCartCode = cartCode;
        }

        String params = httpRequest.getQueryString("cartCode=" + usedCartCode);

        Http.Response httpResponse = http.getApi("/gift/gift-transaksi?" + params);
        if (httpResponse.statusCode == 200) {
            JsonObject dataObject = httpResponse.jsonObject();

            JsonArray rawGift = Json.array(dataObject, "giftTransaksi");
            List<GiftTransaksi> gift = new ArrayList<>();
            for (JsonElement giftItem : rawGift) {
                gift.add(GiftTransaksi.fromJson(Json.obj(giftItem)));
            }

            return gift;
        }

        return giftTransaksi;
    }

    public List<GiftTransaksi> getGiftTransaksi() {
        return getGiftTransaksi(null);
    }
}
