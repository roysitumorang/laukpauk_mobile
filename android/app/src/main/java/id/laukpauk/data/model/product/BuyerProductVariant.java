package id.laukpauk.data.model.product;

import com.squareup.moshi.Json;

/**
 * A variant (selling unit) of a product in {@code GET buyer/products/:id} ({@code data.variants}). Only the keys the
 * app uses are declared; {@code base_price} is hidden from buyers.
 */
public class BuyerProductVariant {
    /** {@code discount_type}: a fixed amount ("cash"); {@link #DISCOUNT_PERCENTAGE} is "persentase". */
    public static final int DISCOUNT_FIXED_AMOUNT = 0;
    public static final int DISCOUNT_PERCENTAGE = 1;

    public String id;
    /** The unit, e.g. "1 kg", "ikat". */
    @Json(name = "stock_unit")
    public String stockUnit;
    @Json(name = "selling_price")
    public long sellingPrice;
    @Json(name = "discount_type")
    public int discountType;
    @Json(name = "discount_type_name")
    public String discountTypeName;
    /** Percent (0-99) for a percentage discount, rupiah for a fixed amount. */
    @Json(name = "discount_rate")
    public long discountRate;
    /** The discount applies from this quantity (0 or 1 = always). */
    @Json(name = "discount_minimum_quantity")
    public long discountMinimumQuantity;
    /** Rupiah off the selling price. */
    @Json(name = "discount_amount")
    public long discountAmount;
    /** Selling price minus the discount. */
    @Json(name = "sale_price")
    public long salePrice;
    public String description;

    public boolean hasDiscount() {
        return discountAmount > 0 && discountRate > 0;
    }

    /** The price to show: {@link #salePrice}, or the selling price when the backend sent no sale price. */
    public long price() {
        return salePrice > 0 ? salePrice : sellingPrice;
    }
}
