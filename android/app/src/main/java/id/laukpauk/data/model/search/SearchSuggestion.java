package id.laukpauk.data.model.search;

import com.squareup.moshi.Json;

/** One row of Konten/pencarian: a product ({@code itemType} "produk"). */
public class SearchSuggestion {
    public String id;
    @Json(name = "jenisItem")
    public String itemType;
    @Json(name = "fotoProduk")
    public String productPhoto;
    @Json(name = "namaProduk")
    public String productName;
}
