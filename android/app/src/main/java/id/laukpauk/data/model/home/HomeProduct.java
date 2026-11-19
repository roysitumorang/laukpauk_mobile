package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;


/**
 * A product tile (the product list, konten items, category products, ListProduk). Only the keys the tiles use
 * are declared; the screens pass lists of these around as JSON text ({@code productListFragment.dataProduk}).
 */
public class HomeProduct {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "foto")
    public String photo;
    @Json(name = "namaKategoriProduk")
    public String productCategoryName;
    @Json(name = "keteranganSingkat")
    public String shortDescription;
    /** Missing = available. */
    @Json(name = "stokAvailable")
    public Boolean stockAvailable;
    /** Active promos; only "is there any" matters (dynamic: array, or false/"" when none). */
    public Object promo;

    public boolean isStockAvailable() {
        return stockAvailable == null || stockAvailable;
    }

    public boolean isInActivePromo() {
        return promo instanceof java.util.List && !((java.util.List<?>) promo).isEmpty();
    }
}
