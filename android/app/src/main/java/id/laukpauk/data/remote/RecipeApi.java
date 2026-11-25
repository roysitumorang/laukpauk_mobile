package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.recipe.RecipeResponses;
import androidx.annotation.Nullable;

import id.laukpauk.data.model.recipe.BuyerRecipeResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/** Recipe endpoints (JSON; ApiClient adds the auth and version headers). */
public interface RecipeApi {

    /** The active recipes, newest first; {@code q} searches the name. Page-based ({@code data.links.next}). */
    @GET("buyer/recipes")
    Call<BuyerRecipeResponses.Page> buyerRecipes(@Query("limit") int limit, @Nullable @Query("q") String q);

    /** A next page: {@code data.links.next}, a full URL. */
    @GET
    Call<BuyerRecipeResponses.Page> buyerRecipesPage(@Url String next);

    @GET("Resep/getResep/{idResep}")
    Call<RecipeResponses.RecipeDetail> getRecipeDetail(@Path("idResep") String recipeId,
                                                     @Query("entitasResep") String recipeEntities);

    @GET("Resep/getResepItemFromThisResep/{idResep}")
    Call<RecipeResponses.ItemList> getRecipeItem(@Path("idResep") String recipeId,
                                                @Query("entitasResepItem") String recipeItemEntities);

    /** Duplicate of the product slice's endpoint (units + prices of a product). */
    @GET("Produk/getHargaProduk")
    Call<RecipeResponses.UnitList> getProductPrice(@Query("idProduk") String productId);

    @POST("Keranjang/belanjaResep")
    Call<RecipeResponses.Shopping> shopRecipe(@Body Map<String, Object> body);
}
