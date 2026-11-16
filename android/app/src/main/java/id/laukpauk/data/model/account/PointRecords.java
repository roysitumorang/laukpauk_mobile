package id.laukpauk.data.model.account;

import com.squareup.moshi.Json;

/** One points history row (User/getRiwayatPoint). createdFrom is "admin" or "transaksi". */
public class PointRecords {
    public static final String CREATED_FROM_TRANSACTION = "transaksi";

    @Json(name = "keterangan")
    public String remarks;
    public String createdFrom;
    @Json(name = "nilai")
    public long value;
}
