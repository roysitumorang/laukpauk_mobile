package id.laukpauk.data.model.product;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;


import java.util.List;

/** A unit product as listed in the product list / the detail screen (fields vary per endpoint). */
public class Product {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "foto")
    public String photo;
    @Json(name = "namaKategoriProduk")
    public String productCategoryName;
    @Json(name = "keterangan")
    public String remarks;
    @Json(name = "keteranganSingkat")
    public String shortDescription;
    /** null when the endpoint doesn't send it (= available). */
    @Json(name = "stokAvailable")
    @Nullable public Boolean stockAvailable;
    /** Active promos; only the size matters (dynamic payload). */
    @Nullable public List<Object> promo;
    /** A pack (paket): "Beli" adds the pack. Set for {@code GET buyer/products} rows. */
    public boolean pack;

    public boolean isStockAvailable() {
        return stockAvailable == null || stockAvailable;
    }

    public boolean isInActivePromo() {
        return promo != null && !promo.isEmpty();
    }
}
