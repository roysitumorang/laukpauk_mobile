package id.laukpauk.data.model.orders;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

/** One line of a buyer transaction ({@code items[]} of {@code buyer/transactions}). */
public class BuyerTransactionItem {
    @Nullable public String id;
    @Json(name = "product_name")
    @Nullable public String productName;
    @Json(name = "product_photo")
    @Nullable public String productPhoto;
    @Json(name = "stock_unit")
    @Nullable public String stockUnit;
    @Json(name = "selling_price")
    public long sellingPrice;
    public long quantity;
    public long subtotal;
    @Json(name = "buyer_note")
    @Nullable public String buyerNote;
    @Json(name = "revised_quantity")
    public long revisedQuantity;
    @Json(name = "revised_subtotal")
    public long revisedSubtotal;
    @Json(name = "revision_note")
    @Nullable public String revisionNote;
    @Json(name = "out_of_stock")
    public boolean outOfStock;
}
