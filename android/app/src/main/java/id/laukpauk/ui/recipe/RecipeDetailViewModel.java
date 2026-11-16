package id.laukpauk.ui.recipe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.recipe.Recipe;
import id.laukpauk.data.model.recipe.RecipeItem;
import id.laukpauk.data.model.recipe.ProductUnit;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.RecipeRepository;

/** State of the recipe detail: the recipe, its ingredient rows (unit, quantity, picked) and the total. */
public class RecipeDetailViewModel extends ViewModel {

    /** Mutable per-ingredient state behind a {@link IngredientRow}. */
    private static final class State {
        final RecipeItem item;
        boolean loaded;
        List<ProductUnit> unit = new ArrayList<>();
        ProductUnit selectedUnit;
        int quantity = 1;
        boolean selected;

        State(RecipeItem item) {
            this.item = item;
        }
    }

    private final RecipeRepository repository;
    private final String recipeId;

    private final MediatorLiveData<Resource<Recipe>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<RecipeItem>>> items = new MediatorLiveData<>();
    private final MutableLiveData<List<IngredientRow>> rows = new MutableLiveData<>(new ArrayList<IngredientRow>());
    /** Sum of the picked ingredients, or -1 when nothing is picked. */
    private final MutableLiveData<Long> total = new MutableLiveData<>(-1L);
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final Map<String, State> states = new LinkedHashMap<>();
    private LiveData<Resource<Recipe>> detailSource;
    private LiveData<Resource<List<RecipeItem>>> itemsSource;
    private int generation;
    private boolean loaded;

    public RecipeDetailViewModel(RecipeRepository repository, String recipeId) {
        this.repository = repository;
        this.recipeId = recipeId;
    }

    public LiveData<Resource<Recipe>> getDetail() {
        return detail;
    }

    public LiveData<Resource<List<RecipeItem>>> getItems() {
        return items;
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
        if (itemsSource != null) {
            items.removeSource(itemsSource);
        }
        states.clear();
        publish();
        detailSource = repository.getRecipeDetail(recipeId);
        detail.addSource(detailSource, detail::setValue);
        itemsSource = repository.getRecipeItem(recipeId);
        items.addSource(itemsSource, res -> {
            items.setValue(res);
            if (mine == generation && res.isSuccess() && res.data != null && states.isEmpty()) {
                for (RecipeItem item : res.data) {
                    State st = new State(item);
                    states.put(String.valueOf(item.id), st);
                    repository.getProductUnit(item.productId, r -> {
                        if (mine != generation) {
                            return;
                        }
                        st.loaded = true;
                        if (r.isSuccess() && r.data != null) {
                            st.unit = r.data;
                        }
                        publish();
                    });
                }
                publish();
            }
        });
    }

    /** The unit dropdown: picking a unit also picks the ingredient (legacy selectedItemCallback). */
    void pickUnit(IngredientRow row, int unitIndex) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null || unitIndex < 0 || unitIndex >= st.unit.size()) {
            return;
        }
        ProductUnit s = st.unit.get(unitIndex);
        st.selectedUnit = s;
        if (s.isExact()) {
            st.quantity = 1;
        }
        st.selected = true;
        publish();
    }

    /** The quantity dropdown (only for "tidak pas" units). */
    void pickQuantity(IngredientRow row, int quantity) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null) {
            return;
        }
        st.quantity = quantity;
        publish();
    }

    /** The cart / trash icon: picks the ingredient (default: first unit) or removes it. */
    void toggle(IngredientRow row) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null) {
            return;
        }
        if (st.selected) {
            st.selected = false;
        } else {
            if (st.selectedUnit == null && !st.unit.isEmpty()) {
                st.selectedUnit = st.unit.get(0);
            }
            if (st.selectedUnit == null) {
                return;
            }
            st.selected = true;
        }
        publish();
    }

    /** "Belanja Resep": adds every picked ingredient to the cart. */
    public void shopping(BaseRepository.ResultCallback<Boolean> callback) {
        List<RecipeRepository.SelectedItem> picked = new ArrayList<>();
        for (State st : states.values()) {
            if (st.selected && st.selectedUnit != null) {
                picked.add(new RecipeRepository.SelectedItem(String.valueOf(st.item.id), st.item.productId,
                        st.selectedUnit.id, st.quantity, st.selectedUnit.netPrice() * st.quantity));
            }
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
            list.add(new IngredientRow(index, st.item, st.loaded, st.unit, st.selectedUnit, st.quantity, st.selected));
            if (st.selected && st.selectedUnit != null) {
                any = true;
                sum += (long) st.selectedUnit.netPrice() * st.quantity;
            }
        }
        rows.setValue(list);
        total.setValue(any ? sum : -1L);
    }
}
