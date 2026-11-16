package id.laukpauk.data.model.product;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;


/** One selectable unit/price row of Produk/getHargaProduk; a promo row when {@link #isPromo} is present. */
public class ProductPrice {
    public String id;
    @Json(name = "nama")
    public String name;
    @Json(name = "harga")
    public int price;
    @Json(name = "diskon")
    public int discount;
    @Json(name = "jenisHarga")
    public String priceType;
    /** Presence-based flag in the legacy code (dynamic payload: any non-null value means promo). */
    @Nullable public Object isPromo;
    @Json(name = "idSatuanProduk")
    public String productUnitId;
    @Json(name = "namaPromo")
    public String promoName;
    @Json(name = "namaSatuan")
    public String unitName;
    public String maxOrder;
    @Json(name = "hargaSatuan")
    public int unitPrice;
    @Json(name = "diskonSatuan")
    public int unitDiscount;
    @Json(name = "diskonPromo")
    public int promoDiscount;
    public String idPromo;

    public boolean promo() {
        return isPromo != null;
    }
}
