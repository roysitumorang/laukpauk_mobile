package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.recipe.RecipeResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Recipe endpoints (JSON; ApiClient adds the auth and version headers). */
public interface ResepApi {

    @GET("Resep/getResep")
    Call<RecipeResponses.ResepList> getResep();

    @GET("Resep/getResep/{idResep}")
    Call<RecipeResponses.ResepDetail> getDetailResep(@Path("idResep") String idResep,
                                                     @Query("entitasResep") String entitasResep);

    @GET("Resep/getResepItemFromThisResep/{idResep}")
    Call<RecipeResponses.ItemList> getResepItem(@Path("idResep") String idResep,
                                                @Query("entitasResepItem") String entitasResepItem);

    /** Duplicate of the product slice's endpoint (units + prices of a product). */
    @GET("Produk/getHargaProduk")
    Call<RecipeResponses.SatuanList> getHargaProduk(@Query("idProduk") String idProduk);

    @POST("Keranjang/belanjaResep")
    Call<RecipeResponses.Belanja> belanjaResep(@Body Map<String, Object> body);
}
