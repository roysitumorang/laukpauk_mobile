package id.laukpauk.data.model.cart;

import com.squareup.moshi.Json;

import androidx.annotation.Nullable;

/** One product line of {@code User/getCart} (also the argument of the edit screen). */
public final class CartProduct {
    public String idCart;
    @Json(name = "idProduk")
    public String productId;
    /** "nonpaket" or "paket". */
    @Json(name = "jenisProduk")
    public String productType;
    @Json(name = "idSatuan")
    public String unitId;
    @Json(name = "namaSatuan")
    public String unitName;
    /** "tidak pas" lets the buyer change the quantity. */
    @Json(name = "jenisSatuan")
    public String unitType;
    @Json(name = "namaProduk")
    public String productName;
    @Json(name = "fotoProduk")
    public String productPhoto;
    @Json(name = "harga")
    public String price;
    @Json(name = "diskon")
    public String discount;
    public String quantity;
    @Json(name = "catatan")
    public String note;
    @Nullable public String idPromo;

    public boolean isPack() {
        return productType != null && productType.equalsIgnoreCase("paket");
    }

    public static int toInt(@Nullable String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(value.trim());
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    /** (harga - diskon) * quantity. */
    public long total() {
        return (long) (toInt(price) - toInt(discount)) * toInt(quantity);
    }
}
