package id.laukpauk.ui.seller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.product.Paket;
import id.laukpauk.data.model.product.Produk;
import id.laukpauk.data.model.shop.Penjual;
import id.laukpauk.data.repository.ProdukRepository;
import id.laukpauk.data.repository.ShopRepository;
import id.laukpauk.ui.market.LowestPriceStore;

/** State of the buyer's seller-shop screen: profile, product / paket lists, selected tab and search text. */
public class PenjualViewModel extends ViewModel {

    private final ShopRepository repository;
    private final String idPenjual;
    private final LowestPriceStore prices;
    private final MediatorLiveData<Resource<Penjual>> detail = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Produk>>> produk = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<List<Paket>>> paket = new MediatorLiveData<>();
    private final MutableLiveData<Integer> tab;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private LiveData<Resource<Penjual>> detailSource;
    private LiveData<Resource<List<Produk>>> produkSource;
    private LiveData<Resource<List<Paket>>> paketSource;

    public PenjualViewModel(ShopRepository repository, ProdukRepository produkRepository, String idPenjual,
                            int initialTab) {
        this.repository = repository;
        this.idPenjual = idPenjual;
        this.prices = new LowestPriceStore(produkRepository);
        this.tab = new MutableLiveData<>(initialTab);
        load();
    }

    public LiveData<Resource<Penjual>> getDetail() {
        return detail;
    }

    public LiveData<Resource<List<Produk>>> getProduk() {
        return produk;
    }

    public LiveData<Resource<List<Paket>>> getPaket() {
        return paket;
    }

    /** 0 = Produk, 1 = Paket. */
    public LiveData<Integer> getTab() {
        return tab;
    }

    public LiveData<String> getQuery() {
        return query;
    }

    public LowestPriceStore getPrices() {
        return prices;
    }

    public String currentUserId() {
        return repository.currentUserId();
    }

    public void selectTab(int value) {
        tab.setValue(value);
    }

    public void setQuery(String text) {
        query.setValue(text == null ? "" : text);
    }

    public void load() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
            produk.removeSource(produkSource);
            paket.removeSource(paketSource);
        }
        detailSource = repository.getDetailPenjual(idPenjual);
        detail.addSource(detailSource, detail::setValue);
        produkSource = repository.getProdukPenjual(idPenjual);
        produk.addSource(produkSource, produk::setValue);
        paketSource = repository.getPaketPenjual(idPenjual);
        paket.addSource(paketSource, paket::setValue);
    }
}
