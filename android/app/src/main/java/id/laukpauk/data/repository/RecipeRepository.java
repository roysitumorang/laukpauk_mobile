package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Paged;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.BuyerProductVariant;
import id.laukpauk.data.model.product.ProductResponses;
import id.laukpauk.data.model.recipe.BuyerRecipe;
import id.laukpauk.data.model.recipe.BuyerRecipeResponses;
import id.laukpauk.data.remote.ProductApi;
import id.laukpauk.data.remote.RecipeApi;

/**
 * Recipes ({@code GET buyer/recipes}, {@code GET buyer/recipes/:id} with guides and items), the variants of an
 * ingredient's product ({@code GET buyer/products/:id}) and "Belanja Resep" ({@code POST buyer/cart}).
 */
public class RecipeRepository extends BaseRepository {

    /** Recipes per page of {@code GET buyer/recipes}. */
    public static final int PAGE_LIMIT = 25;

    private final RecipeApi api;
    private final ProductApi productApi;
    private final CartRepository cart;

    public RecipeRepository(RepositoryDeps deps, CartRepository cart) {
        super(deps);
        this.api = api(RecipeApi.class);
        this.productApi = api(ProductApi.class);
        this.cart = cart;
    }

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

    /** {@code GET buyer/recipes/:id}: the recipe with its guides and items; ERROR 404 when it is not there. */
    public LiveData<Resource<BuyerRecipe>> getBuyerRecipe(String recipeId) {
        return load(api.buyerRecipe(recipeId == null ? "" : recipeId),
                (BuyerRecipeResponses.One body) -> body == null ? null : body.data);
    }

    /** The variants of an ingredient's product ({@code GET buyer/products/:id}); empty when it has none. */
    public void getProductVariants(String productId, ResultCallback<List<BuyerProductVariant>> callback) {
        enqueue(productApi.buyerProduct(productId == null ? "" : productId),
                (ProductResponses.BuyerProductDetail body) -> body == null || body.data == null
                        || body.data.variants == null ? new ArrayList<BuyerProductVariant>() : body.data.variants,
                callback);
    }

    /**
     * "Belanja Resep": adds the picked variants and quantities to the buyer's cart in one {@code POST buyer/cart};
     * SUCCESS(true) when it was saved.
     */
    public void shopRecipe(Map<String, Long> quantities, ResultCallback<Boolean> callback) {
        cart.addVariants(quantities, r -> callback.onResult(r.isSuccess()
                ? Resource.success(true) : Resource.<Boolean>error(r.message, r.code)));
    }
}
