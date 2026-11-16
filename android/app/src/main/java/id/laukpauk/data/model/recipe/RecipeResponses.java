package id.laukpauk.data.model.recipe;

import com.squareup.moshi.Json;

import java.util.List;

/** Response envelopes of the recipe endpoints. */
public final class RecipeResponses {
    private RecipeResponses() {}

    public static class RecipeList {
        @Json(name = "dataResep")
        public List<Recipe> recipeData;
    }

    public static class RecipeDetail {
        /** null when the server answers false. */
        @Json(name = "dataResep")
        public Recipe recipeData;
    }

    public static class ItemList {
        @Json(name = "dataResepItem")
        public List<RecipeItem> recipeItemData;
    }

    public static class UnitList {
        @Json(name = "hargaProduk")
        public List<ProductUnit> productPrice;
    }

    public static class Shopping {
        @Json(name = "statusBelanjaResep")
        public boolean recipeShoppingStatus;
    }
}
