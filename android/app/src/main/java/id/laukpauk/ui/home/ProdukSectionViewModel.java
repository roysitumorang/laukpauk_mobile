package id.laukpauk.ui.home;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.home.HomeProduk;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.data.repository.HomeRepository;

/** The first products of the product list. Reloads whenever the delivery selection changes. */
public class ProdukSectionViewModel extends ViewModel {

    private final HomeRepository home;
    private final DeliveryRepository delivery;

    private final MediatorLiveData<Resource<List<HomeProduk>>> produk = new MediatorLiveData<>();
    private LiveData<Resource<List<HomeProduk>>> source;
    private String lastKey;

    public ProdukSectionViewModel(HomeRepository home, DeliveryRepository delivery) {
        this.home = home;
        this.delivery = delivery;
        produk.addSource(delivery.selection(), selection -> {
            if (!key(selection).equals(lastKey)) {
                reload();
            }
        });
    }

    public LiveData<Resource<List<HomeProduk>>> getProduk() {
        return produk;
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
        if (source != null) {
            produk.removeSource(source);
        }
        source = home.getProduk();
        produk.addSource(source, produk::setValue);
    }
}
