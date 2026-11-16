package id.laukpauk.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.model.home.HomeProduct;

/** State of ListProduk: the received products and the name filter. */
public class ProductListViewModel extends ViewModel {

    private final List<HomeProduct> allProduct;
    private final MutableLiveData<List<HomeProduct>> product;
    private String query = "";

    public ProductListViewModel(List<HomeProduct> product) {
        this.allProduct = product;
        this.product = new MutableLiveData<>(product);
    }

    public LiveData<List<HomeProduct>> getProduct() {
        return product;
    }

    public String getQuery() {
        return query;
    }

    /** Filters the products by name like the legacy search box. */
    public void setQuery(String q) {
        query = q == null ? "" : q;
        String lower = query.toLowerCase(Locale.getDefault());
        List<HomeProduct> out = new ArrayList<>();
        for (HomeProduct p : allProduct) {
            if (p.name != null && p.name.toLowerCase(Locale.getDefault()).contains(lower)) {
                out.add(p);
            }
        }
        product.setValue(out);
    }
}
