package id.laukpauk.ui.recipe;

import androidx.annotation.Nullable;

import java.util.List;

import id.laukpauk.data.model.recipe.RecipeItem;
import id.laukpauk.data.model.recipe.ProductUnit;

/** Immutable UI state of one ingredient row (legacy ResepItemItem). */
final class IngredientRow {
    final int index;
    final RecipeItem item;
    final boolean unitLoaded;
    final List<ProductUnit> unit;
    @Nullable final ProductUnit selectedUnit;
    final int quantity;
    final boolean selected;

    IngredientRow(int index, RecipeItem item, boolean unitLoaded, List<ProductUnit> unit,
             @Nullable ProductUnit selectedUnit, int quantity, boolean selected) {
        this.index = index;
        this.item = item;
        this.unitLoaded = unitLoaded;
        this.unit = unit;
        this.selectedUnit = selectedUnit;
        this.quantity = quantity;
        this.selected = selected;
    }

    int netPrice() {
        return selectedUnit == null ? 0 : selectedUnit.netPrice();
    }

    boolean sameAs(IngredientRow o) {
        return index == o.index && unitLoaded == o.unitLoaded && unit == o.unit
                && selectedUnit == o.selectedUnit && quantity == o.quantity && selected == o.selected;
    }
}
