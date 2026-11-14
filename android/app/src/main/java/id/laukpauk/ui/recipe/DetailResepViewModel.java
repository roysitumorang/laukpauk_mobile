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
import id.laukpauk.data.model.recipe.Resep;
import id.laukpauk.data.model.recipe.ResepItem;
import id.laukpauk.data.model.recipe.SatuanProduk;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.ResepRepository;

/** State of the recipe detail: the recipe, its ingredient rows (unit, quantity, picked) and the total. */
public class DetailResepViewModel extends ViewModel {

    /** Mutable per-ingredient state behind a {@link BahanRow}. */
    private static final class State {
        final ResepItem item;
        boolean loaded;
        List<SatuanProduk> satuan = new ArrayList<>();
        SatuanProduk selectedSatuan;
        int quantity = 1;
        boolean selected;

        State(ResepItem item) {
            this.item = item;
        }
    }

    private final ResepRepository repository;
    private final String idResep;

    private final MediatorLiveData<Resource<Resep>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<ResepItem>>> items = new MediatorLiveData<>();
    private final MutableLiveData<List<BahanRow>> rows = new MutableLiveData<>(new ArrayList<BahanRow>());
    /** Sum of the picked ingredients, or -1 when nothing is picked. */
    private final MutableLiveData<Long> total = new MutableLiveData<>(-1L);
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final Map<String, State> states = new LinkedHashMap<>();
    private LiveData<Resource<Resep>> detailSource;
    private LiveData<Resource<List<ResepItem>>> itemsSource;
    private int generation;
    private boolean loaded;

    public DetailResepViewModel(ResepRepository repository, String idResep) {
        this.repository = repository;
        this.idResep = idResep;
    }

    public LiveData<Resource<Resep>> getDetail() {
        return detail;
    }

    public LiveData<Resource<List<ResepItem>>> getItems() {
        return items;
    }

    LiveData<List<BahanRow>> getRows() {
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
        detailSource = repository.getDetailResep(idResep);
        detail.addSource(detailSource, detail::setValue);
        itemsSource = repository.getResepItem(idResep);
        items.addSource(itemsSource, res -> {
            items.setValue(res);
            if (mine == generation && res.isSuccess() && res.data != null && states.isEmpty()) {
                for (ResepItem item : res.data) {
                    State st = new State(item);
                    states.put(String.valueOf(item.id), st);
                    repository.getSatuanProduk(item.idProduk, r -> {
                        if (mine != generation) {
                            return;
                        }
                        st.loaded = true;
                        if (r.isSuccess() && r.data != null) {
                            st.satuan = r.data;
                        }
                        publish();
                    });
                }
                publish();
            }
        });
    }

    /** The unit dropdown: picking a unit also picks the ingredient (legacy selectedItemCallback). */
    void pickSatuan(BahanRow row, int satuanIndex) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null || satuanIndex < 0 || satuanIndex >= st.satuan.size()) {
            return;
        }
        SatuanProduk s = st.satuan.get(satuanIndex);
        st.selectedSatuan = s;
        if (s.isPas()) {
            st.quantity = 1;
        }
        st.selected = true;
        publish();
    }

    /** The quantity dropdown (only for "tidak pas" units). */
    void pickJumlah(BahanRow row, int quantity) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null) {
            return;
        }
        st.quantity = quantity;
        publish();
    }

    /** The cart / trash icon: picks the ingredient (default: first unit) or removes it. */
    void toggle(BahanRow row) {
        State st = states.get(String.valueOf(row.item.id));
        if (st == null) {
            return;
        }
        if (st.selected) {
            st.selected = false;
        } else {
            if (st.selectedSatuan == null && !st.satuan.isEmpty()) {
                st.selectedSatuan = st.satuan.get(0);
            }
            if (st.selectedSatuan == null) {
                return;
            }
            st.selected = true;
        }
        publish();
    }

    /** "Belanja Resep": adds every picked ingredient to the cart. */
    public void belanja(BaseRepository.ResultCallback<Boolean> callback) {
        List<ResepRepository.SelectedItem> picked = new ArrayList<>();
        for (State st : states.values()) {
            if (st.selected && st.selectedSatuan != null) {
                picked.add(new ResepRepository.SelectedItem(String.valueOf(st.item.id), st.item.idProduk,
                        st.selectedSatuan.id, st.quantity, st.selectedSatuan.hargaBersih() * st.quantity));
            }
        }
        busy.setValue(true);
        repository.belanjaResep(picked, result -> {
            busy.setValue(false);
            callback.onResult(result);
        });
    }

    private void publish() {
        List<BahanRow> list = new ArrayList<>();
        long sum = 0;
        boolean any = false;
        int index = 0;
        for (State st : states.values()) {
            index++;
            list.add(new BahanRow(index, st.item, st.loaded, st.satuan, st.selectedSatuan, st.quantity, st.selected));
            if (st.selected && st.selectedSatuan != null) {
                any = true;
                sum += (long) st.selectedSatuan.hargaBersih() * st.quantity;
            }
        }
        rows.setValue(list);
        total.setValue(any ? sum : -1L);
    }
}
