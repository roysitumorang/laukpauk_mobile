package id.laukpauk.data.model.product;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;

/** One selectable unit/price row of Produk/getHargaProduk; a promo row when {@link #isPromo} is present. */
public class HargaProduk {
    public String id;
    public String nama;
    public int harga;
    public int diskon;
    public String jenisHarga;
    /** Presence-based flag in the legacy code (dynamic payload: any non-null value means promo). */
    @Nullable public JsonElement isPromo;
    public String idSatuanProduk;
    public String namaPromo;
    public String namaSatuan;
    public String maxOrder;
    public int hargaSatuan;
    public int diskonSatuan;
    public int diskonPromo;
    public String idPromo;

    public boolean promo() {
        return isPromo != null;
    }
}
