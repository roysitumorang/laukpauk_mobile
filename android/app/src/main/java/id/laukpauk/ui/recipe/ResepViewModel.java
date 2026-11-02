package id.laukpauk.ui.recipe;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.recipe.Resep;
import id.laukpauk.data.repository.ResepRepository;

/** State of the Resep screen: the recipes and the name filter. */
public class ResepViewModel extends ViewModel {

    private final ResepRepository repository;
    private final MediatorLiveData<Resource<List<Resep>>> state = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<Resep>> visible = new MediatorLiveData<>();
    private LiveData<Resource<List<Resep>>> source;

    public ResepViewModel(ResepRepository repository) {
        this.repository = repository;
        visible.addSource(state, r -> filter());
        visible.addSource(query, q -> filter());
    }

    public LiveData<Resource<List<Resep>>> getState() {
        return state;
    }

    public LiveData<List<Resep>> getVisible() {
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
        source = repository.getResep();
        state.addSource(source, state::setValue);
    }

    private void filter() {
        Resource<List<Resep>> res = state.getValue();
        List<Resep> all = res == null || res.data == null ? new ArrayList<Resep>() : res.data;
        String q = getQuery().toLowerCase(Locale.ROOT);
        List<Resep> out = new ArrayList<>();
        for (Resep r : all) {
            if (q.isEmpty() || (r.nama == null ? "" : r.nama).toLowerCase(Locale.ROOT).contains(q)) {
                out.add(r);
            }
        }
        visible.setValue(out);
    }
}
