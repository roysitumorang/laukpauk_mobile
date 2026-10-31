package id.laukpauk.element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import id.laukpauk.core.Json;

/** Port of lib/element-class/Gift.dart. All fields are nullable like the Dart ones. */
public final class Gift {
    public final Integer id;
    public final String foto;
    public final String nama;
    public final Integer minimalBelanja;
    public final String berlakuMulai;
    public final String berlakuSampai;
    public final Integer quota;
    public final String deskripsi;
    public final Integer createdBy;
    public final String createdAt;
    public final Integer updatedBy;
    public final String updatedAt;

    public Gift(Integer id, String foto, String nama, Integer minimalBelanja, String berlakuMulai,
                String berlakuSampai, Integer quota, String deskripsi, Integer createdBy,
                String createdAt, Integer updatedBy, String updatedAt) {
        this.id = id;
        this.foto = foto;
        this.nama = nama;
        this.minimalBelanja = minimalBelanja;
        this.berlakuMulai = berlakuMulai;
        this.berlakuSampai = berlakuSampai;
        this.quota = quota;
        this.deskripsi = deskripsi;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    /** Dart: int.tryParse(json[key]) - null when missing or not an integer. */
    private static Integer tryParse(JsonObject json, String key) {
        JsonElement e = Json.get(json, key);
        if (Json.isNull(e) || !e.isJsonPrimitive()) {
            return null;
        }
        if (e.getAsJsonPrimitive().isNumber()) {
            return (int) e.getAsDouble();
        }
        try {
            return Integer.parseInt(e.getAsString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** Dart: Gift.fromJson(Map). */
    public static Gift fromJson(JsonObject json) {
        return new Gift(
                tryParse(json, "id"),
                Json.strOrNull(json, "foto"),
                Json.strOrNull(json, "nama"),
                tryParse(json, "minimalBelanja"),
                Json.strOrNull(json, "berlakuMulai"),
                Json.strOrNull(json, "berlakuSampai"),
                tryParse(json, "quota"),
                Json.strOrNull(json, "deskripsi"),
                tryParse(json, "createdBy"),
                Json.strOrNull(json, "createdAt"),
                // Dart: json['updatedBy'] == null ? null : int.tryParse(json['updatedBy'])
                Json.has(json, "updatedBy") ? tryParse(json, "updatedBy") : null,
                Json.strOrNull(json, "updatedAt"));
    }

    /** Dart: toJson(). */
    public JsonObject toJson() {
        return Json.of(
                "id", id,
                "foto", foto,
                "nama", nama,
                "minimalBelanja", minimalBelanja,
                "berlakuMulai", berlakuMulai,
                "berlakuSampai", berlakuSampai,
                "quota", quota,
                "deskripsi", deskripsi,
                "createdBy", createdBy,
                "createdAt", createdAt,
                "updatedBy", updatedBy,
                "updatedAt", updatedAt);
    }
}
