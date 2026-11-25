package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import id.laukpauk.data.Paged;
import id.laukpauk.data.model.recipe.BuyerRecipe;
import id.laukpauk.data.model.recipe.BuyerRecipeResponses;
import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/recipes} (the Resep screen's pages) and {@code GET buyer/recipes/:id} (the detail). */
public class BuyerRecipesTest {

    @Test
    public void pageHasTheRecipesAndTheNextUrl() throws Exception {
        String json = "{\"status_code\":200,\"data\":{\"links\":{\"next\":\"https://sadia.id/v1/buyer/recipes?limit=25&page=2\"},"
                + "\"info\":{\"limit\":25,\"page\":1,\"pages\":2,\"total\":30},\"rows\":[{\"id\":\"r1\",\"record_status\":1,"
                + "\"photo\":\"https://sadia.id/photos/r1.jpg\",\"name\":\"Sayur Asem\",\"deleted_name\":null,"
                + "\"description\":\"Segar\",\"instagram_link\":\"https://instagram.com/p/x\","
                + "\"facebook_link\":\"\",\"created_at\":\"2026-11-25T21:00:00+07:00\"}]}}";
        Paged<BuyerRecipe> page = RecipeRepository.page(ApiClient.createMoshi()
                .adapter(BuyerRecipeResponses.Page.class).fromJson(json));
        assertEquals(1, page.items.size());
        assertEquals("https://sadia.id/v1/buyer/recipes?limit=25&page=2", page.next);
        BuyerRecipe r = page.items.get(0);
        assertEquals("r1", r.id);
        assertEquals("Sayur Asem", r.name);
        assertEquals("https://sadia.id/photos/r1.jpg", r.photo);
        assertEquals("Segar", r.description);
        assertEquals("https://instagram.com/p/x", r.instagramLink);
        assertEquals("", r.facebookLink);
    }

    @Test
    public void lastPageHasNoNext() throws Exception {
        Paged<BuyerRecipe> page = RecipeRepository.page(ApiClient.createMoshi()
                .adapter(BuyerRecipeResponses.Page.class).fromJson("{\"data\":{\"links\":{\"next\":\"\"},\"rows\":[]}}"));
        assertEquals(0, page.items.size());
        assertNull(page.next);
        assertEquals(0, RecipeRepository.page(null).items.size());
    }

    @Test
    public void detailHasTheGuidesAndItems() throws Exception {
        String json = "{\"status_code\":200,\"data\":{\"id\":\"r1\",\"record_status\":1,\"photo\":\"r1.jpg\","
                + "\"name\":\"Sayur Asem\",\"description\":\"Segar\",\"instagram_link\":\"\",\"facebook_link\":\"\","
                + "\"guides\":[{\"id\":\"g1\",\"record_status\":1,\"recipe_id\":\"r1\",\"instruction\":\"Rebus air\"}],"
                + "\"items\":[{\"id\":\"i1\",\"record_status\":1,\"recipe_id\":\"r1\",\"product_id\":\"p1\","
                + "\"product_name\":\"Asam Jawa\",\"product_photo\":\"p1.jpg\",\"product_ready_stock\":true,"
                + "\"product_variant_id\":\"v1\",\"product_variant_stock_unit\":\"gram\",\"quantity\":2,"
                + "\"note\":\"secukupnya\"}]}}";
        BuyerRecipe r = ApiClient.createMoshi().adapter(BuyerRecipeResponses.One.class).fromJson(json).data;
        assertEquals("Sayur Asem", r.name);
        assertEquals(1, r.guides.size());
        assertEquals("Rebus air", r.guides.get(0).instruction);
        BuyerRecipe.Item item = r.items.get(0);
        assertEquals("p1", item.productId);
        assertEquals("Asam Jawa", item.productName);
        assertEquals(true, item.productReadyStock);
        assertEquals("v1", item.productVariantId);
        assertEquals("gram", item.productVariantStockUnit);
        assertEquals(2, item.quantity);
        assertEquals("secukupnya", item.note);
    }
}
