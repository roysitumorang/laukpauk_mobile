package id.laukpauk.data.model.product;

import com.squareup.moshi.Json;

/** A package (paket) of products. */
public class Pack {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "foto")
    public String photo;
    @Json(name = "harga")
    public int price;
    @Json(name = "diskon")
    public int discount;
    @Json(name = "keterangan")
    public String remarks;
}
