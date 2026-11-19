package id.laukpauk.data.model.search;

import com.squareup.moshi.Json;

/** One search result: a product of {@code GET buyer/products?q=} ({@code itemType} "produk" or "paket"). */
public class SearchSuggestion {
    public String id;
    @Json(name = "jenisItem")
    public String itemType;
    @Json(name = "fotoProduk")
    public String productPhoto;
    @Json(name = "namaProduk")
    public String productName;
}
