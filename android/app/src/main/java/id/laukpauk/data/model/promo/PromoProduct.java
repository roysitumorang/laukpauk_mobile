package id.laukpauk.data.model.promo;

import id.laukpauk.util.Json;

/** One promo product of {@code Promo/getPromoAroundOfThisLatLong} (a card of the Promo grid). */
public class PromoProduct {
    public String idPromo;
    public String idPromoItem;
    @com.squareup.moshi.Json(name = "idProduk")
    public String productId;
    /** The price (unit) id, sent as unitId when adding to the cart. */
    @com.squareup.moshi.Json(name = "idHargaProduk")
    public String productPriceId;
    @com.squareup.moshi.Json(name = "namaProduk")
    public String productName;
    @com.squareup.moshi.Json(name = "keteranganSingkat")
    public String shortDescription;
    @com.squareup.moshi.Json(name = "fotoProduk")
    public String productPhoto;
    public String maxOrder;
    /** Numeric strings from the server; use the int accessors. */
    @com.squareup.moshi.Json(name = "hargaProduk")
    public String productPrice;
    @com.squareup.moshi.Json(name = "diskonProduk")
    public String productDiscount;
    @com.squareup.moshi.Json(name = "diskonPromo")
    public String promoDiscount;

    public int grossPrice() {
        return Json.parseInt(productPrice, 0);
    }

    public int discount() {
        return Json.parseInt(productDiscount, 0);
    }

    public int promoDiscountInt() {
        return Json.parseInt(promoDiscount, 0);
    }

    /** Price after the product discount and the promo discount. */
    public int promoNetPrice() {
        return grossPrice() - discount() - promoDiscountInt();
    }
}
