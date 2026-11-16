package id.laukpauk.data.model.checkout;

import com.squareup.moshi.Json;

/** A buyer voucher (GET User/getVoucher -> listVoucher[]). */
public class Voucher {
    public String id;
    @Json(name = "nama")
    public String name;
    /** File name in ServerPath.uploadGambarVoucher/compress. */
    @Json(name = "foto")
    public String photo;
    @Json(name = "minimumBelanja")
    public long minimumShopping;
    @Json(name = "nilai")
    public long value;
    /** What the voucher reduces, e.g. "ongkir" or "belanja". */
    @Json(name = "jenis")
    public String type;
    @Json(name = "kode")
    public String code;
    public String maximumUsage;
}
