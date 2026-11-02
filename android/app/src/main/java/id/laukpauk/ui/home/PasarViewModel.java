package id.laukpauk.ui.home;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.model.home.Pasar;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.data.repository.HomeRepository;

/**
 * Legacy PasarSekitarSaya: the markets (around the chosen delivery location, or all) filtered by a name search,
 * and optionally the first products of the nearest markets. Reloads whenever the delivery selection changes.
 */
public class PasarViewModel extends ViewModel {

    private final HomeRepository home;
    private final DeliveryRepository delivery;
    @Nullable
    private final Integer limit;
    private final boolean withProduk;

    private final MediatorLiveData<Resource<List<Pasar>>> pasar = new MediatorLiveData<>();
    private final MediatorLiveData<List<ProdukPasarSection>> sections = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> showJarak = new MutableLiveData<>(false);

    private LiveData<Resource<List<Pasar>>> pasarSource;
    private final List<LiveData<?>> produkSources = new ArrayList<>();
    private String query = "";
    private String lastKey;
    private int generation;

    /**
     * @param limit      max markets (null = all)
     * @param withProduk also load the products of the nearest markets (2 when there are 3 or more, else all)
     */
    public PasarViewModel(HomeRepository home, DeliveryRepository delivery, @Nullable Integer limit,
                          boolean withProduk) {
        this.home = home;
        this.delivery = delivery;
        this.limit = limit;
        this.withProduk = withProduk;
        pasar.addSource(delivery.selection(), selection -> {
            if (!key(selection).equals(lastKey)) {
                reload();
            }
        });
    }

    public LiveData<Resource<List<Pasar>>> getPasar() {
        return pasar;
    }

    public LiveData<List<ProdukPasarSection>> getSections() {
        return sections;
    }

    /** Whether the distance is shown on the market tiles (a delivery selection is set). */
    public LiveData<Boolean> getShowJarak() {
        return showJarak;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String value) {
        query = value == null ? "" : value;
        reload();
    }

    private static String key(@Nullable DeliverySelection s) {
        if (s == null || !s.isSet) {
            return "unset";
        }
        return s.latitude + "|" + s.longitude + "|" + s.jadwalDeliveryCode;
    }

    /** First observation: loads once (later changes come from the delivery selection). */
    public void start() {
        if (lastKey == null) {
            reload();
        }
    }

    public void reload() {
        lastKey = key(delivery.current());
        final int mine = ++generation;
        if (pasarSource != null) {
            pasar.removeSource(pasarSource);
        }
        clearProdukSources();
        showJarak.setValue(delivery.current().isSet);
        pasarSource = home.getDataPasar(limit, query);
        pasar.addSource(pasarSource, res -> {
            if (mine != generation) {
                return;
            }
            pasar.setValue(res);
            if (withProduk && res.isSuccess()) {
                loadProduk(res.data, mine);
            } else if (!res.isLoading()) {
                sections.setValue(new ArrayList<ProdukPasarSection>());
            }
        });
    }

    private void clearProdukSources() {
        for (LiveData<?> s : produkSources) {
            sections.removeSource(s);
        }
        produkSources.clear();
    }

    private void loadProduk(@Nullable List<Pasar> list, final int mine) {
        final List<ProdukPasarSection> current = new ArrayList<>();
        if (list != null) {
            int count = list.size() >= 3 ? 2 : list.size();
            for (int i = 0; i < count; i++) {
                current.add(new ProdukPasarSection(list.get(i), null));
            }
        }
        sections.setValue(new ArrayList<>(current));
        for (int i = 0; i < current.size(); i++) {
            final int index = i;
            final Pasar p = current.get(i).pasar;
            final LiveData<Resource<List<HomeProduk>>> source = home.getProdukInThisPasar(p.id);
            produkSources.add(source);
            sections.addSource(source, res -> {
                if (mine != generation || res.isLoading()) {
                    return;
                }
                sections.removeSource(source);
                produkSources.remove(source);
                List<HomeProduk> produk = res.isSuccess() && res.data != null ? res.data
                        : new ArrayList<HomeProduk>();
                current.set(index, new ProdukPasarSection(p, produk));
                sections.setValue(new ArrayList<>(current));
            });
        }
    }
}
