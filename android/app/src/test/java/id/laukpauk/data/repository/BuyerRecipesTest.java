package id.laukpauk.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import id.laukpauk.data.Paged;
import id.laukpauk.data.model.recipe.BuyerRecipe;
import id.laukpauk.data.model.recipe.BuyerRecipeResponses;
import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/recipes}: the Resep screen's pages. */
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
}
