package id.laukpauk.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.PenjualItem;

/** State of ListProduk: the received products/sellers, the name filter and which of the two is shown. */
public class ListProdukViewModel extends ViewModel {

    private final List<HomeProduk> allProduk;
    private final List<PenjualItem> allPenjual;
    private final MutableLiveData<List<HomeProduk>> produk;
    private final MutableLiveData<List<PenjualItem>> penjual;
    private final MutableLiveData<Boolean> showPenjual = new MutableLiveData<>(false);
    private String query = "";

    public ListProdukViewModel(List<HomeProduk> produk, List<PenjualItem> penjual) {
        this.allProduk = produk;
        this.allPenjual = penjual;
        this.produk = new MutableLiveData<>(produk);
        this.penjual = new MutableLiveData<>(penjual);
    }

    public LiveData<List<HomeProduk>> getProduk() {
        return produk;
    }

    public LiveData<List<PenjualItem>> getPenjual() {
        return penjual;
    }

    public LiveData<Boolean> getShowPenjual() {
        return showPenjual;
    }

    public boolean hasPenjual() {
        return !allPenjual.isEmpty();
    }

    public String getQuery() {
        return query;
    }

    public void toggleView() {
        showPenjual.setValue(!Boolean.TRUE.equals(showPenjual.getValue()));
    }

    /** Filters the shown list (products by name, sellers by shop name) like the legacy search box. */
    public void setQuery(String q) {
        query = q == null ? "" : q;
        String lower = query.toLowerCase(Locale.getDefault());
        if (Boolean.TRUE.equals(showPenjual.getValue())) {
            List<PenjualItem> out = new ArrayList<>();
            for (PenjualItem p : allPenjual) {
                if (p.namaToko != null && p.namaToko.toLowerCase(Locale.getDefault()).contains(lower)) {
                    out.add(p);
                }
            }
            penjual.setValue(out);
        } else {
            List<HomeProduk> out = new ArrayList<>();
            for (HomeProduk p : allProduk) {
                if (p.nama != null && p.nama.toLowerCase(Locale.getDefault()).contains(lower)) {
                    out.add(p);
                }
            }
            produk.setValue(out);
        }
    }
}
