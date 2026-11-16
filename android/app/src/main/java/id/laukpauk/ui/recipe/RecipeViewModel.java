package id.laukpauk.ui.recipe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.recipe.Recipe;
import id.laukpauk.data.repository.RecipeRepository;

/** State of the Resep screen: the recipes and the name filter. */
public class RecipeViewModel extends ViewModel {

    private final RecipeRepository repository;
    private final MediatorLiveData<Resource<List<Recipe>>> state = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<Recipe>> visible = new MediatorLiveData<>();
    private LiveData<Resource<List<Recipe>>> source;

    public RecipeViewModel(RecipeRepository repository) {
        this.repository = repository;
        visible.addSource(state, r -> filter());
        visible.addSource(query, q -> filter());
    }

    public LiveData<Resource<List<Recipe>>> getState() {
        return state;
    }

    public LiveData<List<Recipe>> getVisible() {
        return visible;
    }

    public String getQuery() {
        String q = query.getValue();
        return q == null ? "" : q;
    }

    public void search(String text) {
        query.setValue(text == null ? "" : text);
    }

    public boolean isLoaded() {
        return source != null;
    }

    public void load() {
        if (source != null) {
            state.removeSource(source);
        }
        source = repository.getRecipe();
        state.addSource(source, state::setValue);
    }

    private void filter() {
        Resource<List<Recipe>> res = state.getValue();
        List<Recipe> all = res == null || res.data == null ? new ArrayList<Recipe>() : res.data;
        String q = getQuery().toLowerCase(Locale.ROOT);
        List<Recipe> out = new ArrayList<>();
        for (Recipe r : all) {
            if (q.isEmpty() || (r.name == null ? "" : r.name).toLowerCase(Locale.ROOT).contains(q)) {
                out.add(r);
            }
        }
        visible.setValue(out);
    }
}
