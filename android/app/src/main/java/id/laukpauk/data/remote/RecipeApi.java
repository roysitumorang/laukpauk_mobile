package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import id.laukpauk.data.model.recipe.BuyerRecipeResponses;
import retrofit2.Call;
import retrofit2.http.GET;
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

    /** One active recipe with its {@code guides} and {@code items}; 404 when it is not there. */
    @GET("buyer/recipes/{id}")
    Call<BuyerRecipeResponses.One> buyerRecipe(@Path("id") String id);
}
