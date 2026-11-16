package id.laukpauk.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.home.ProductCategory;
import id.laukpauk.data.repository.HomeRepository;

/** All product categories with a client-side name filter (legacy SemuaKategoriProduk). */
public class AllProductCategoriesViewModel extends ViewModel {

    private final HomeRepository home;
    private final MediatorLiveData<Resource<List<ProductCategory>>> source = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<ProductCategory>> filtered = new MediatorLiveData<>();
    private LiveData<Resource<List<ProductCategory>>> current;

    public AllProductCategoriesViewModel(HomeRepository home) {
        this.home = home;
        filtered.addSource(source, r -> update());
        filtered.addSource(query, q -> update());
    }

    /** Whole-list state (LOADING only the first time, SUCCESS afterwards). */
    public LiveData<Resource<List<ProductCategory>>> getState() {
        return source;
    }

    public LiveData<List<ProductCategory>> getFiltered() {
        return filtered;
    }

    public String getQuery() {
        return query.getValue() == null ? "" : query.getValue();
    }

    public void setQuery(String q) {
        query.setValue(q == null ? "" : q);
    }

    public void load() {
        if (current != null) {
            source.removeSource(current);
        }
        current = home.getProductCategory();
        source.addSource(current, source::setValue);
    }

    private void update() {
        Resource<List<ProductCategory>> res = source.getValue();
        if (res == null || !res.isSuccess() || res.data == null) {
            filtered.setValue(new ArrayList<ProductCategory>());
            return;
        }
        String q = getQuery().toLowerCase(Locale.getDefault());
        List<ProductCategory> out = new ArrayList<>();
        for (ProductCategory k : res.data) {
            if (k.name != null && k.name.toLowerCase(Locale.getDefault()).contains(q)) {
                out.add(k);
            }
        }
        filtered.setValue(out);
    }
}
