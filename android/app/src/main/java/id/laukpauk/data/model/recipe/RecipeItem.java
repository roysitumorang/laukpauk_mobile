package id.laukpauk.data.model.recipe;

import com.squareup.moshi.Json;

/** One ingredient of a recipe. */
public class RecipeItem {
    public String id;
    @Json(name = "namaProduk")
    public String productName;
    @Json(name = "takaran")
    public String portion;
    @Json(name = "idProduk")
    public String productId;
}
