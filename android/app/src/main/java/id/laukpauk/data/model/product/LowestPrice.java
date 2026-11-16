package id.laukpauk.data.model.product;

import com.squareup.moshi.Json;

/** Produk/getLowestPriceOfThisProduct: the cheapest unit of a product. */
public class LowestPrice {
    /** Marker for "this product has no price list". */
    public static final LowestPrice NONE = new LowestPrice();

    @Json(name = "harga")
    public int price;
    @Json(name = "diskon")
    public int discount;
    @Json(name = "hargaBersih")
    public int netPrice;

    /** Discount percentage of the gross price (0 when none). */
    public double discountPercentage() {
        return price == 0 ? 0 : (double) discount / price * 100;
    }
}
