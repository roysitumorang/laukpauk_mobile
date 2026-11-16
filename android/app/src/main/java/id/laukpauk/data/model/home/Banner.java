package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;

/** A hero slider banner (Banner/getBanner). */
public class Banner {
    public String id;
    @Json(name = "gambar")
    public String image;
    @Json(name = "nama")
    public String name;
    @Json(name = "keterangan")
    public String remarks;
}
