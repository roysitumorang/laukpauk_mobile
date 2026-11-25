package id.laukpauk.ui.recipe;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Paged;
import id.laukpauk.data.Resource;
import id.laukpauk.data.model.recipe.BuyerRecipe;
import id.laukpauk.data.repository.RecipeRepository;

/**
 * State of the Resep screen ({@code GET buyer/recipes}): the name searched ({@code q}) and the list, which grows page
 * by page ({@code data.links.next}).
 */
public class RecipeViewModel extends ViewModel {

    private final RecipeRepository repository;
    private final MutableLiveData<Resource<List<BuyerRecipe>>> state = new MutableLiveData<>();
    private final List<BuyerRecipe> rows = new ArrayList<>();
    private String query = "";
    @Nullable private String next;
    private boolean loadingMore;
    private boolean loaded;
    /** Bumped on every reload, so a late answer for an old search is dropped. */
    private int generation;

    public RecipeViewModel(RecipeRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<List<BuyerRecipe>>> getState() {
        return state;
    }

    public String getQuery() {
        return query;
    }

    /** Searches the recipe names (the backend's {@code q}); "" lists all. */
    public void search(String text) {
        query = text == null ? "" : text.trim();
        load();
    }

    public boolean isLoaded() {
        return loaded;
    }

    /** Reloads the first page for the current search. */
    public void load() {
        loaded = true;
        final int gen = ++generation;
        rows.clear();
        next = null;
        loadingMore = false;
        state.setValue(Resource.loading());
        repository.getBuyerRecipes(query, r -> {
            if (gen != generation) {
                return;
            }
            if (!r.isSuccess() || r.data == null) {
                state.setValue(Resource.error(r.message, r.code));
                return;
            }
            take(r.data);
        });
    }

    /** The next page, when there is one and none is loading. */
    public void loadMore() {
        if (next == null || loadingMore) {
            return;
        }
        loadingMore = true;
        final int gen = generation;
        repository.getBuyerRecipesPage(next, r -> {
            if (gen != generation) {
                return;
            }
            loadingMore = false;
            if (r.isSuccess() && r.data != null) {
                take(r.data);
            }
        });
    }

    private void take(Paged<BuyerRecipe> page) {
        rows.addAll(page.items);
        next = page.next;
        state.setValue(Resource.success(new ArrayList<>(rows)));
    }
}
