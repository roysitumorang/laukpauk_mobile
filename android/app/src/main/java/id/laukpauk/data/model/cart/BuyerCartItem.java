package id.laukpauk.data.model.cart;

import com.squareup.moshi.Json;

/**
 * One row of {@link BuyerCart}: a product variant, its quantity and the variant's product and price attributes
 * ({@code GET/POST buyer/cart}, {@code data.items}). Only the keys the app uses are declared.
 */
public class BuyerCartItem {
    public String id;
    @Json(name = "product_variant_id")
    public String productVariantId;
    public long quantity;

    @Json(name = "product_id")
    public String productId;
    @Json(name = "product_name")
    public String productName;
    /** Full image URL. */
    @Json(name = "product_photo")
    public String productPhoto;
    @Json(name = "product_category_name")
    public String productCategoryName;
    @Json(name = "product_ready_stock")
    public boolean productReadyStock = true;
    @Json(name = "stock_unit")
    public String stockUnit;
    @Json(name = "selling_price")
    public long sellingPrice;
    @Json(name = "discount_type")
    public int discountType;
    @Json(name = "discount_rate")
    public long discountRate;
    /** The discount applies from this quantity (0 or 1 = always). */
    @Json(name = "discount_minimum_quantity")
    public long discountMinimumQuantity;
    @Json(name = "discount_amount")
    public long discountAmount;
    @Json(name = "sale_price")
    public long salePrice;
    public String description;

    /** True when the variant's discount applies at this quantity. */
    public boolean discounted() {
        return discountAmount > 0 && salePrice > 0 && salePrice < sellingPrice
                && quantity >= Math.max(1, discountMinimumQuantity);
    }

    /** Price of one unit at this quantity: the sale price when discounted, otherwise the selling price. */
    public long unitPrice() {
        return discounted() ? salePrice : sellingPrice;
    }

    /** {@link #unitPrice()} × quantity. */
    public long lineTotal() {
        return unitPrice() * quantity;
    }
}
