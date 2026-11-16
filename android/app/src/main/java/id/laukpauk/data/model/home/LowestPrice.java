package id.laukpauk.data.model.home;

import com.squareup.moshi.Json;

/** Cheapest unit price of a product (Produk/getLowestPriceOfThisProduct). */
public class LowestPrice {
    @Json(name = "diskon")
    public long discount;
    @Json(name = "harga")
    public long price;
    @Json(name = "hargaBersih")
    public long netPrice;

    /** Discount in percent of the gross price (0 when none). */
    public double discountPercentage() {
        return price <= 0 ? 0 : (double) discount / price * 100;
    }
}
