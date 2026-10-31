package id.laukpauk.element;

import com.google.gson.JsonObject;

import id.laukpauk.core.Json;

/** Port of lib/element-class/Keranjang.dart. Every field is a nullable String, as in Dart. */
public final class Keranjang {
    public final String id;
    public final String cartCode;
    public final String dayDelivery;
    public final String jadwalDeliveryCode;
    public final String idUser;
    public final String idPromo;
    public final String diskonPromo;
    public final String idPasarPenjual;
    public final String idPenjual;
    public final String idProduk;
    public final String jenisProduk;
    public final String idSatuan;
    public final String harga;
    public final String diskon;
    public final String quantity;
    public final String catatan;

    public Keranjang(String id, String cartCode, String dayDelivery, String jadwalDeliveryCode,
                     String idUser, String idPromo, String diskonPromo, String idPasarPenjual,
                     String idPenjual, String idProduk, String jenisProduk, String idSatuan,
                     String harga, String diskon, String quantity, String catatan) {
        this.id = id;
        this.cartCode = cartCode;
        this.dayDelivery = dayDelivery;
        this.jadwalDeliveryCode = jadwalDeliveryCode;
        this.idUser = idUser;
        this.idPromo = idPromo;
        this.diskonPromo = diskonPromo;
        this.idPasarPenjual = idPasarPenjual;
        this.idPenjual = idPenjual;
        this.idProduk = idProduk;
        this.jenisProduk = jenisProduk;
        this.idSatuan = idSatuan;
        this.harga = harga;
        this.diskon = diskon;
        this.quantity = quantity;
        this.catatan = catatan;
    }

    /** Dart: Keranjang.fromJson(Map). */
    public static Keranjang fromJson(JsonObject json) {
        return new Keranjang(
                Json.strOrNull(json, "id"),
                Json.strOrNull(json, "cartCode"),
                Json.strOrNull(json, "dayDelivery"),
                Json.strOrNull(json, "jadwalDeliveryCode"),
                Json.strOrNull(json, "idUser"),
                Json.strOrNull(json, "idPromo"),
                Json.strOrNull(json, "diskonPromo"),
                Json.strOrNull(json, "idPasarPenjual"),
                Json.strOrNull(json, "idPenjual"),
                Json.strOrNull(json, "idProduk"),
                Json.strOrNull(json, "jenisProduk"),
                Json.strOrNull(json, "idSatuan"),
                Json.strOrNull(json, "harga"),
                Json.strOrNull(json, "diskon"),
                Json.strOrNull(json, "quantity"),
                Json.strOrNull(json, "catatan"));
    }

    /** Dart: toJson(). */
    public JsonObject toJson() {
        return Json.of(
                "id", id,
                "cartCode", cartCode,
                "dayDelivery", dayDelivery,
                "jadwalDeliveryCode", jadwalDeliveryCode,
                "idUser", idUser,
                "idPromo", idPromo,
                "diskonPromo", diskonPromo,
                "idPasarPenjual", idPasarPenjual,
                "idPenjual", idPenjual,
                "idProduk", idProduk,
                "jenisProduk", jenisProduk,
                "idSatuan", idSatuan,
                "harga", harga,
                "diskon", diskon,
                "quantity", quantity,
                "catatan", catatan);
    }
}
