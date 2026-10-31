package id.laukpauk.model;

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
    private GiftTransaksiModel() {}

    /** Dart: getGiftTransaksi({String? cartCode}); pass null for the stored cart code. */
    public static List<GiftTransaksi> getGiftTransaksi(String cartCode) {
        List<GiftTransaksi> giftTransaksi = new ArrayList<>();

        String usedCartCode = LocalStorage.getCartCode();
        if (cartCode != null) {
            usedCartCode = cartCode;
        }

        String params = HTTPRequest.getQueryString("cartCode=" + usedCartCode);

        Http.Response httpResponse = Http.getApi("/gift/gift-transaksi?" + params);
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

    public static List<GiftTransaksi> getGiftTransaksi() {
        return getGiftTransaksi(null);
    }
}
