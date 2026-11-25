package id.laukpauk.ui.recipe;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.BuyerProductVariant;
import id.laukpauk.data.model.recipe.BuyerRecipe;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.RecipeRepository;

/**
 * State of the recipe detail ({@code GET buyer/recipes/:id}): the recipe with its guides, its ingredient rows
 * (variant, quantity, picked) and the total of the picked ones.
 */
public class RecipeDetailViewModel extends ViewModel {

    /** Mutable per-ingredient state behind a {@link IngredientRow}. */
    private static final class State {
        final BuyerRecipe.Item item;
        boolean loaded;
        List<BuyerProductVariant> variants = new ArrayList<>();
        BuyerProductVariant selectedVariant;
        long quantity;
        boolean selected;

        State(BuyerRecipe.Item item) {
            this.item = item;
            this.quantity = Math.max(1, item.quantity);
        }
    }

    private final RecipeRepository repository;
    private final String recipeId;

    private final MediatorLiveData<Resource<BuyerRecipe>> detail = new MediatorLiveData<>();
    private final MutableLiveData<List<IngredientRow>> rows = new MutableLiveData<>(new ArrayList<IngredientRow>());
    /** Sum of the picked ingredients, or -1 when nothing is picked. */
    private final MutableLiveData<Long> total = new MutableLiveData<>(-1L);
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final Map<String, State> states = new LinkedHashMap<>();
    private LiveData<Resource<BuyerRecipe>> detailSource;
    private int generation;
    private boolean loaded;

    public RecipeDetailViewModel(RecipeRepository repository, String recipeId) {
        this.repository = repository;
        this.recipeId = recipeId;
    }

    public LiveData<Resource<BuyerRecipe>> getDetail() {
        return detail;
    }

    LiveData<List<IngredientRow>> getRows() {
        return rows;
    }

    public LiveData<Long> getTotal() {
        return total;
    }

    public LiveData<Boolean> getBusy() {
        return busy;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load() {
        loaded = true;
        final int mine = ++generation;
        if (detailSource != null) {
            detail.removeSource(detailSource);
        }
        states.clear();
        publish();
        detailSource = repository.getBuyerRecipe(recipeId);
        detail.addSource(detailSource, res -> {
            detail.setValue(res);
            if (mine != generation || res == null || !res.isSuccess() || res.data == null
                    || res.data.items == null || !states.isEmpty()) {
                return;
            }
            for (BuyerRecipe.Item item : res.data.items) {
                State st = new State(item);
                states.put(String.valueOf(item.id), st);
                if (!item.productReadyStock) {
                    st.loaded = true;
                    continue;
                }
                repository.getProductVariants(item.productId, r -> {
                    if (mine != generation) {
                        return;
                    }
                    st.loaded = true;
                    if (r.isSuccess() && r.data != null) {
                        st.variants = r.data;
                        st.selectedVariant = recipeVariant(r.data, item.productVariantId);
                    }
                    publish();
                });
            }
            publish();
        });
    }

    /** The variant the recipe names, else the product's first variant; null when there are none. */
    @Nullable
    static BuyerProductVariant recipeVariant(List<BuyerProductVariant> variants, @Nullable String variantId) {
        for (BuyerProductVariant v : variants) {
            if (v.id != null && v.id.equals(variantId)) {
                return v;
            }
        }
        return variants.isEmpty() ? null : variants.get(0);
    }

    /** The cooking steps as numbered lines ("1. …"); blank steps are skipped, empty when there are none. */
    static String guidesText(@Nullable List<BuyerRecipe.Guide> guides) {
        StringBuilder sb = new StringBuilder();
        if (guides == null) {
            return "";
        }
        int n = 0;
        for (BuyerRecipe.Guide g : guides) {
            if (g == null || g.instruction == null || g.instruction.trim().isEmpty()) {
                continue;
            }
            if (n > 0) {
                sb.append('\n');
            }
            sb.append(++n).append(". ").append(g.instruction.trim());
        }
        return sb.toString();
    }

    /** The unit dropdown: picking a variant also picks the ingredient. */
    void pickVariant(IngredientRow row, int variantIndex) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null || variantIndex < 0 || variantIndex >= st.variants.size()) {
            return;
        }
        st.selectedVariant = st.variants.get(variantIndex);
        st.selected = true;
        publish();
    }

    /** The quantity dropdown. */
    void pickQuantity(IngredientRow row, long quantity) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null || quantity < 1) {
            return;
        }
        st.quantity = quantity;
        publish();
    }

    /** The cart / trash icon: picks the ingredient (with the recipe's variant) or removes it. */
    void toggle(IngredientRow row) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null) {
            return;
        }
        if (st.selected) {
            st.selected = false;
        } else {
            if (st.selectedVariant == null) {
                st.selectedVariant = recipeVariant(st.variants, st.item.productVariantId);
            }
            if (st.selectedVariant == null) {
                return;
            }
            st.selected = true;
        }
        publish();
    }

    /** "Belanja Resep": adds every picked ingredient to the cart ({@code POST buyer/cart}). */
    public void shopping(BaseRepository.ResultCallback<Boolean> callback) {
        Map<String, Long> picked = new LinkedHashMap<>();
        for (State st : states.values()) {
            if (st.selected && st.selectedVariant != null && st.selectedVariant.id != null) {
                Long q = picked.get(st.selectedVariant.id);
                picked.put(st.selectedVariant.id, (q == null ? 0 : q) + st.quantity);
            }
        }
        if (picked.isEmpty()) {
            return;
        }
        busy.setValue(true);
        repository.shopRecipe(picked, result -> {
            busy.setValue(false);
            callback.onResult(result);
        });
    }

    private void publish() {
        List<IngredientRow> list = new ArrayList<>();
        long sum = 0;
        boolean any = false;
        int index = 0;
        for (State st : states.values()) {
            index++;
            list.add(new IngredientRow(index, st.item, st.loaded, st.variants, st.selectedVariant, st.quantity,
                    st.selected));
            if (st.selected && st.selectedVariant != null) {
                any = true;
                sum += st.selectedVariant.price() * st.quantity;
            }
        }
        rows.setValue(list);
        total.setValue(any ? sum : -1L);
    }
}
