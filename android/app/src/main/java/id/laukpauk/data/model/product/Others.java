package id.laukpauk.data.model.product;

import com.squareup.moshi.Json;

/** "Produk Lainnya" / "Paket Lainnya" entry. */
public class Others {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "foto")
    public String photo;
}
