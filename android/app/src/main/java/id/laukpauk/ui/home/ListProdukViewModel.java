package id.laukpauk.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.model.home.HomeProduk;

/** State of ListProduk: the received products and the name filter. */
public class ListProdukViewModel extends ViewModel {

    private final List<HomeProduk> allProduk;
    private final MutableLiveData<List<HomeProduk>> produk;
    private String query = "";

    public ListProdukViewModel(List<HomeProduk> produk) {
        this.allProduk = produk;
        this.produk = new MutableLiveData<>(produk);
    }

    public LiveData<List<HomeProduk>> getProduk() {
        return produk;
    }

    public String getQuery() {
        return query;
    }

    /** Filters the products by name like the legacy search box. */
    public void setQuery(String q) {
        query = q == null ? "" : q;
        String lower = query.toLowerCase(Locale.getDefault());
        List<HomeProduk> out = new ArrayList<>();
        for (HomeProduk p : allProduk) {
            if (p.nama != null && p.nama.toLowerCase(Locale.getDefault()).contains(lower)) {
                out.add(p);
            }
        }
        produk.setValue(out);
    }
}
