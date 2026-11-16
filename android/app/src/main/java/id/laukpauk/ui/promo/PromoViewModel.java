package id.laukpauk.ui.promo;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.promo.PromoProduct;
import id.laukpauk.data.repository.PromoRepository;

/** State of the Promo screen: the promo products around the chosen address and the search filter. */
public class PromoViewModel extends ViewModel {

    private final PromoRepository repository;

    private final MediatorLiveData<Resource<List<PromoProduct>>> state = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<PromoProduct>> visible = new MediatorLiveData<>();
    private LiveData<Resource<List<PromoProduct>>> source;
    private String loadedKey;

    public PromoViewModel(PromoRepository repository) {
        this.repository = repository;
        visible.addSource(state, r -> filter());
        visible.addSource(query, q -> filter());
    }

    public LiveData<DeliverySelection> getSelection() {
        return repository.selection();
    }

    public LiveData<Resource<List<PromoProduct>>> getState() {
        return state;
    }

    /** The loaded promo products filtered by the search text (product name contains it). */
    public LiveData<List<PromoProduct>> getVisible() {
        return visible;
    }

    public String getQuery() {
        String q = query.getValue();
        return q == null ? "" : q;
    }

    public void search(String text) {
        query.setValue(text == null ? "" : text);
    }

    /** Loads when the delivery selection changed since the last load (or nothing was loaded yet); none is needed. */
    public void onSelection(@Nullable DeliverySelection selection) {
        String key = selection == null || !selection.isSet ? "unset"
                : selection.latitude + "|" + selection.longitude + "|" + selection.deliveryScheduleCode;
        if (source == null || !key.equals(loadedKey)) {
            loadedKey = key;
            refresh();
        }
    }

    public void refresh() {
        if (source != null) {
            state.removeSource(source);
        }
        source = repository.getPromoProduct();
        state.addSource(source, state::setValue);
    }

    private void filter() {
        Resource<List<PromoProduct>> res = state.getValue();
        List<PromoProduct> all = res == null || res.data == null ? new ArrayList<PromoProduct>() : res.data;
        String q = getQuery().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            visible.setValue(all);
            return;
        }
        List<PromoProduct> out = new ArrayList<>();
        for (PromoProduct p : all) {
            String name = p.productName == null ? "" : p.productName;
            if (name.toLowerCase(Locale.ROOT).contains(q)) {
                out.add(p);
            }
        }
        visible.setValue(out);
    }
}
