package id.laukpauk.data.model.product;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;

import java.util.List;

/** A unit product as listed in a market / a seller's shop / the detail screen (fields vary per endpoint). */
public class Produk {
    public String id;
    public String nama;
    public String foto;
    public String idPenjual;
    public String namaToko;
    public String namaKategoriProduk;
    public String keterangan;
    public String keteranganSingkat;
    /** null when the endpoint doesn't send it (= available). */
    @Nullable public Boolean stokAvailable;
    /** Active promos; only the size matters (dynamic payload). */
    @Nullable public List<JsonElement> promo;

    public boolean isStockAvailable() {
        return stokAvailable == null || stokAvailable;
    }

    public boolean isInActivePromo() {
        return promo != null && !promo.isEmpty();
    }
}
