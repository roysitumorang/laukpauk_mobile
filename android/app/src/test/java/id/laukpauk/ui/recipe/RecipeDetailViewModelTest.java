package id.laukpauk.ui.recipe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import id.laukpauk.data.model.product.BuyerProductVariant;
import id.laukpauk.data.model.recipe.BuyerRecipe;

/** The recipe detail's guide text and the variant an ingredient starts with. */
public class RecipeDetailViewModelTest {

    private static BuyerRecipe.Guide guide(String instruction) {
        BuyerRecipe.Guide g = new BuyerRecipe.Guide();
        g.instruction = instruction;
        return g;
    }

    private static BuyerProductVariant variant(String id) {
        BuyerProductVariant v = new BuyerProductVariant();
        v.id = id;
        return v;
    }

    @Test
    public void guidesAreNumberedSkippingBlanks() {
        assertEquals("1. Rebus air\n2. Masukkan sayur",
                RecipeDetailViewModel.guidesText(Arrays.asList(guide(" Rebus air "), guide(" "), guide("Masukkan sayur"))));
        assertEquals("", RecipeDetailViewModel.guidesText(null));
    }

    @Test
    public void startsWithTheRecipesVariantElseTheFirst() {
        List<BuyerProductVariant> vs = Arrays.asList(variant("v1"), variant("v2"));
        assertEquals("v2", RecipeDetailViewModel.recipeVariant(vs, "v2").id);
        assertEquals("v1", RecipeDetailViewModel.recipeVariant(vs, "gone").id);
        assertNull(RecipeDetailViewModel.recipeVariant(new ArrayList<BuyerProductVariant>(), "v1"));
    }
}
