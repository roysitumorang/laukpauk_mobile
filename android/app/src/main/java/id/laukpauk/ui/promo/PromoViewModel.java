package id.laukpauk.ui.promo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.promo.ProdukPromo;
import id.laukpauk.data.repository.PromoRepository;

/** State of the Promo screen: the promo products around the chosen address and the search filter. */
public class PromoViewModel extends ViewModel {

    private final PromoRepository repository;

    private final MediatorLiveData<Resource<List<ProdukPromo>>> state = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<ProdukPromo>> visible = new MediatorLiveData<>();
    private LiveData<Resource<List<ProdukPromo>>> source;
    private String loadedKey;

    public PromoViewModel(PromoRepository repository) {
        this.repository = repository;
        visible.addSource(state, r -> filter());
        visible.addSource(query, q -> filter());
    }

    public LiveData<DeliverySelection> getSelection() {
        return repository.selection();
    }

    public LiveData<Resource<List<ProdukPromo>>> getState() {
        return state;
    }

    /** The loaded promo products filtered by the search text (product name contains it). */
    public LiveData<List<ProdukPromo>> getVisible() {
        return visible;
    }

    public String getQuery() {
        String q = query.getValue();
        return q == null ? "" : q;
    }

    public void search(String text) {
        query.setValue(text == null ? "" : text);
    }

    /** Loads when the delivery selection changed since the last load (or nothing was loaded yet). */
    public void onSelection(DeliverySelection selection) {
        if (selection == null || !selection.isSet) {
            return;
        }
        String key = selection.latitude + "|" + selection.longitude + "|" + selection.jadwalDeliveryCode;
        if (source == null || !key.equals(loadedKey)) {
            loadedKey = key;
            refresh();
        }
    }

    public void refresh() {
        if (source != null) {
            state.removeSource(source);
        }
        source = repository.getProdukPromo();
        state.addSource(source, state::setValue);
    }

    private void filter() {
        Resource<List<ProdukPromo>> res = state.getValue();
        List<ProdukPromo> all = res == null || res.data == null ? new ArrayList<ProdukPromo>() : res.data;
        String q = getQuery().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            visible.setValue(all);
            return;
        }
        List<ProdukPromo> out = new ArrayList<>();
        for (ProdukPromo p : all) {
            String nama = p.namaProduk == null ? "" : p.namaProduk;
            if (nama.toLowerCase(Locale.ROOT).contains(q)) {
                out.add(p);
            }
        }
        visible.setValue(out);
    }
}
