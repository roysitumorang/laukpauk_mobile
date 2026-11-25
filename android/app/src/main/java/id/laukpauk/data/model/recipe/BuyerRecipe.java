package id.laukpauk.data.model.recipe;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.List;

/** A recipe of {@code GET buyer/recipes} ({@code data.rows[]}) and {@code GET buyer/recipes/:id} (with guides and items). */
public class BuyerRecipe {
    @Nullable public String id;
    @Nullable public String name;
    /** A full URL, or a file name as stored. */
    @Nullable public String photo;
    @Nullable public String description;
    @Json(name = "instagram_link")
    @Nullable public String instagramLink;
    @Json(name = "facebook_link")
    @Nullable public String facebookLink;
    /** The cooking steps, in order (detail only). */
    @Nullable public List<Guide> guides;
    /** The ingredients, in order (detail only). */
    @Nullable public List<Item> items;

    /** One cooking step. */
    public static class Guide {
        @Nullable public String id;
        @Nullable public String instruction;
    }

    /** One ingredient: a product variant and how much of it the recipe needs. */
    public static class Item {
        @Nullable public String id;
        @Json(name = "product_id")
        @Nullable public String productId;
        @Json(name = "product_name")
        @Nullable public String productName;
        @Json(name = "product_photo")
        @Nullable public String productPhoto;
        @Json(name = "product_ready_stock")
        public boolean productReadyStock;
        @Json(name = "product_variant_id")
        @Nullable public String productVariantId;
        @Json(name = "product_variant_stock_unit")
        @Nullable public String productVariantStockUnit;
        public long quantity;
        @Nullable public String note;
    }
}
