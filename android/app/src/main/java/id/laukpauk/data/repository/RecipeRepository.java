package id.laukpauk.data.repository;

import id.laukpauk.util.Json;

import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.recipe.BuyerRecipeResponses;
import id.laukpauk.data.model.recipe.BuyerRecipe;
import id.laukpauk.data.Paged;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.recipe.RecipeResponses;
import id.laukpauk.data.model.recipe.Recipe;
import id.laukpauk.data.model.recipe.RecipeItem;
import id.laukpauk.data.model.recipe.ProductUnit;
import id.laukpauk.data.remote.RecipeApi;

/** Recipes, their ingredients and "Belanja Resep" (legacy ResepModel). */
public class RecipeRepository extends BaseRepository {

    private static final String RECIPE_ENTITIES = "id, nama, foto, keterangan, igLink, fbLink";
    private static final String RECIPE_ITEM_ENTITIES = "id, namaProduk, takaran, idProduk";
    private static final String DEFAULT_DAY = "Hari Pengataran";

    /** One ingredient picked for "Belanja Resep". */
    public static final class SelectedItem {
        public final String idItem;
        public final String productId;
        public final String unitId;
        public final int quantity;
        public final int subTotal;

        public SelectedItem(String idItem, String productId, String unitId, int quantity, int subTotal) {
            this.idItem = idItem;
            this.productId = productId;
            this.unitId = unitId;
            this.quantity = quantity;
            this.subTotal = subTotal;
        }
    }

    private final RecipeApi api;
    private final DeliveryRepository delivery;

    public RecipeRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(RecipeApi.class);
        this.delivery = delivery;
    }

    /** Recipes per page of {@code GET buyer/recipes}. */
    public static final int PAGE_LIMIT = 25;

    /** {@code GET buyer/recipes}: the first page; {@code q} (the name searched) or null. */
    public void getBuyerRecipes(@Nullable String q, ResultCallback<Paged<BuyerRecipe>> callback) {
        String query = q == null || q.trim().isEmpty() ? null : q.trim();
        enqueue(api.buyerRecipes(PAGE_LIMIT, query), RecipeRepository::page, callback);
    }

    /** The page at {@code next} ({@code data.links.next} of the previous page). */
    public void getBuyerRecipesPage(String next, ResultCallback<Paged<BuyerRecipe>> callback) {
        enqueue(api.buyerRecipesPage(next), RecipeRepository::page, callback);
    }

    /** Rows and next-page URL of a {@code GET buyer/recipes} page. */
    public static Paged<BuyerRecipe> page(@Nullable BuyerRecipeResponses.Page body) {
        BuyerRecipeResponses.PageData data = body == null ? null : body.data;
        if (data == null) {
            return Paged.empty();
        }
        List<BuyerRecipe> rows = new ArrayList<>();
        if (data.rows != null) {
            for (BuyerRecipe r : data.rows) {
                if (r != null) {
                    rows.add(r);
                }
            }
        }
        return new Paged<>(rows, data.links == null ? null : data.links.next);
    }

    /** SUCCESS with null data when the recipe doesn't exist. */
    public LiveData<Resource<Recipe>> getRecipeDetail(String recipeId) {
        return load(api.getRecipeDetail(recipeId, RECIPE_ENTITIES), body -> body == null ? null : body.recipeData);
    }

    public LiveData<Resource<List<RecipeItem>>> getRecipeItem(String recipeId) {
        return load(api.getRecipeItem(recipeId, RECIPE_ITEM_ENTITIES),
                body -> body == null || body.recipeItemData == null
                        ? Collections.<RecipeItem>emptyList() : body.recipeItemData);
    }

    /** Units + prices of an ingredient's product (empty list when there are none). */
    public void getProductUnit(String productId, ResultCallback<List<ProductUnit>> callback) {
        enqueue(api.getProductPrice(productId),
                (RecipeResponses.UnitList body) -> body == null || body.productPrice == null
                        ? Collections.<ProductUnit>emptyList() : body.productPrice,
                callback);
    }

    /**
     * Adds all picked ingredients to the cart. SUCCESS(true) when the server's recipeShoppingStatus is true;
     * The delivery schedule is sent only when one is chosen.
     */
    public void shopRecipe(List<SelectedItem> items, ResultCallback<Boolean> callback) {
        DeliverySelection s = delivery.current();
        Map<String, Object> selected = Json.newObject();
        for (SelectedItem item : items) {
            Map<String, Object> o = Json.newObject();
            o.put("idProduk", item.productId);
            o.put("quantity", String.valueOf(item.quantity));
            o.put("idSatuan", item.unitId);
            o.put("subTotal", item.subTotal);
            selected.put(item.idItem, o);
        }
        String day = s.deliveryScheduleCode == null ? null : s.dayDelivery != null ? s.dayDelivery : DEFAULT_DAY;
        enqueue(api.shopRecipe(body("cartCode", session.getCartCode(), "selectedItems", selected, "dayDelivery",
                day, "jadwalDeliveryCode", s.deliveryScheduleCode)),
                (RecipeResponses.Shopping body) -> body != null && body.recipeShoppingStatus, callback);
    }
}
