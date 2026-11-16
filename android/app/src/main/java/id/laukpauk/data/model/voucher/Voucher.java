package id.laukpauk.data.model.voucher;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;

/** A voucher (list rows carry only some fields; the detail has them all). */
public final class Voucher {
    public String id;
    @Json(name = "foto")
    public String photo;
    @Json(name = "nama")
    public String name;
    /** "ongkir" or "belanja". */
    @Json(name = "jenis")
    public String type;
    @Json(name = "nilai")
    public String value;
    @Json(name = "kode")
    public String code;
    @Json(name = "minimumBelanja")
    public String minimumShopping;
    public String maximumUsage;
    public String usageCount;
    @Json(name = "berlakuMulai")
    public String validFrom;
    @Json(name = "berlakuSampai")
    public String validUntil;
    @Json(name = "deskripsi")
    public String description;

    public static int toInt(@Nullable String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(value.trim());
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }
}
