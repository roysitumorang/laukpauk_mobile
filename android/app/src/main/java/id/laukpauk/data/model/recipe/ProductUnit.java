package id.laukpauk.data.model.recipe;

import id.laukpauk.util.Json;

/** A unit/price of a product ({@code Produk/getHargaProduk}). */
public class ProductUnit {
    public String id;
    @com.squareup.moshi.Json(name = "nama")
    public String name;
    @com.squareup.moshi.Json(name = "harga")
    public String price;
    @com.squareup.moshi.Json(name = "diskon")
    public String discount;
    /** "pas" or "tidak pas". */
    @com.squareup.moshi.Json(name = "jenisHarga")
    public String priceType;

    public int netPrice() {
        return Json.parseInt(price, 0) - Json.parseInt(discount, 0);
    }

    public boolean isExact() {
        return priceType != null && priceType.toLowerCase(java.util.Locale.ROOT).equals("pas");
    }

    public boolean isNotExact() {
        return priceType != null && priceType.toLowerCase(java.util.Locale.ROOT).equals("tidak pas");
    }
}
