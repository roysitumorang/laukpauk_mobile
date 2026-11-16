package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;

/** A product category. */
public class ProductCategory {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "gambar")
    public String image;
}
