package id.laukpauk.data.model.cart;

import com.squareup.moshi.Json;

/** One row of {@link BuyerCart}: a product variant and its quantity. */
public class BuyerCartItem {
    public String id;
    @Json(name = "product_variant_id")
    public String productVariantId;
    public long quantity;
}
