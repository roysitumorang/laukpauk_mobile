package id.laukpauk.data.model.product;

import androidx.annotation.Nullable;


import java.util.List;

/** A unit product as listed in the product list / the detail screen (fields vary per endpoint). */
public class Produk {
    public String id;
    public String nama;
    public String foto;
    public String namaKategoriProduk;
    public String keterangan;
    public String keteranganSingkat;
    /** null when the endpoint doesn't send it (= available). */
    @Nullable public Boolean stokAvailable;
    /** Active promos; only the size matters (dynamic payload). */
    @Nullable public List<Object> promo;

    public boolean isStockAvailable() {
        return stokAvailable == null || stokAvailable;
    }

    public boolean isInActivePromo() {
        return promo != null && !promo.isEmpty();
    }
}
