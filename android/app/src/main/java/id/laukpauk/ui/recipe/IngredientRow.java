package id.laukpauk.ui.recipe;

import androidx.annotation.Nullable;

import java.util.List;

import id.laukpauk.data.model.product.BuyerProductVariant;
import id.laukpauk.data.model.recipe.BuyerRecipe;

/** Immutable UI state of one ingredient row: the recipe item, its product's variants and what the buyer picked. */
final class IngredientRow {
    final int index;
    final BuyerRecipe.Item item;
    final boolean variantsLoaded;
    final List<BuyerProductVariant> variants;
    @Nullable final BuyerProductVariant selectedVariant;
    final long quantity;
    final boolean selected;

    IngredientRow(int index, BuyerRecipe.Item item, boolean variantsLoaded, List<BuyerProductVariant> variants,
                  @Nullable BuyerProductVariant selectedVariant, long quantity, boolean selected) {
        this.index = index;
        this.item = item;
        this.variantsLoaded = variantsLoaded;
        this.variants = variants;
        this.selectedVariant = selectedVariant;
        this.quantity = quantity;
        this.selected = selected;
    }

    long price() {
        return selectedVariant == null ? 0 : selectedVariant.price();
    }

    boolean sameAs(IngredientRow o) {
        return index == o.index && variantsLoaded == o.variantsLoaded && variants == o.variants
                && selectedVariant == o.selectedVariant && quantity == o.quantity && selected == o.selected;
    }
}
