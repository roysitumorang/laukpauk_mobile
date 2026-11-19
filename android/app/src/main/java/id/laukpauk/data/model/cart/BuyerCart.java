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

    /** The cart's value: the sum of the items' {@link BuyerCartItem#lineTotal()}. */
    public long total() {
        long sum = 0;
        if (items != null) {
            for (BuyerCartItem item : items) {
                if (item != null) {
                    sum += item.lineTotal();
                }
            }
        }
        return sum;
    }
}
