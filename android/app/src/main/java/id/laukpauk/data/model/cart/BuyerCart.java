package id.laukpauk.data.model.cart;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.List;

/** The signed-in buyer's cart of {@code GET/POST buyer/cart} ({@code data}): one row per product variant. */
public class BuyerCart {
    /** All zeros while the buyer has no cart yet. */
    @Nullable public String id;
    @Json(name = "user_id")
    @Nullable public String userId;
    /** Total quantity of all items. */
    public long quantity;
    @Nullable public List<BuyerCartItem> items;
}
