package id.laukpauk.element;

import com.google.gson.JsonObject;

import id.laukpauk.core.Json;

/** Port of lib/element-class/GiftTransaksi.dart. */
public final class GiftTransaksi {
    public final String id;
    public final String idGift;
    public final String cartCode;
    public final String createdAt;

    public GiftTransaksi(String id, String idGift, String cartCode, String createdAt) {
        this.id = id;
        this.idGift = idGift;
        this.cartCode = cartCode;
        this.createdAt = createdAt;
    }

    /** Dart: GiftTransaksi.fromJson(Map). */
    public static GiftTransaksi fromJson(JsonObject json) {
        return new GiftTransaksi(
                Json.strOrNull(json, "id"),
                Json.strOrNull(json, "idGift"),
                Json.strOrNull(json, "cartCode"),
                Json.strOrNull(json, "createdAt"));
    }

    /** Dart: toJson(). */
    public JsonObject toJson() {
        return Json.of(
                "id", id,
                "idGift", idGift,
                "cartCode", cartCode,
                "createdAt", createdAt);
    }
}
