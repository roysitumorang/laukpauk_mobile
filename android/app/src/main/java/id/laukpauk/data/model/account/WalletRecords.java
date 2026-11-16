package id.laukpauk.data.model.account;

import com.squareup.moshi.Json;

/** One wallet history row (User/getRiwayatDompet). */
public class WalletRecords {
    @Json(name = "jenis")
    public String type;
    @Json(name = "keterangan")
    public String remarks;
    public String createdAt;
    @Json(name = "nilai")
    public long value;
}
