package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.Transaksi;
import id.laukpauk.data.model.orders.TransaksiResponses;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.CartRepository;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.data.repository.TransaksiRepository;

/** State of Detail Transaksi: the transaction, the bank account (for BCA transfers) and delivered-item count. */
public class DetailTransaksiViewModel extends ViewModel {

    private final TransaksiRepository repository;
    private final DeliveryRepository delivery;
    private final SettingRepository settings;
    private final CartRepository cart;
    private final String idTransaksi;

    private final MediatorLiveData<Resource<Transaksi>> detail = new MediatorLiveData<>();
    private LiveData<Resource<Transaksi>> detailSource;
    private final MediatorLiveData<Resource<Integer>> diantar = new MediatorLiveData<>();
    private LiveData<Resource<Integer>> diantarSource;
    /** {nomorRekening, pemilikNomorRekening}; null until the settings answered. */
    private final MutableLiveData<String[]> rekening = new MutableLiveData<>();

    public DetailTransaksiViewModel(TransaksiRepository repository, DeliveryRepository delivery,
                                    SettingRepository settings, CartRepository cart, String idTransaksi) {
        this.repository = repository;
        this.delivery = delivery;
        this.settings = settings;
        this.cart = cart;
        this.idTransaksi = idTransaksi;
        loadRekening();
        load();
    }

    public LiveData<Resource<Transaksi>> getDetail() {
        return detail;
    }

    public LiveData<Resource<Integer>> getDiantar() {
        return diantar;
    }

    public LiveData<String[]> getRekening() {
        return rekening;
    }

    private void loadRekening() {
        final String[] values = {"", ""};
        settings.getSettingValue("nomorRekeningLP", r1 -> {
            values[0] = SettingRepository.asString(r1.data);
            settings.getSettingValue("pemilikNomorRekeningLP", r2 -> {
                values[1] = SettingRepository.asString(r2.data);
                rekening.setValue(values);
            });
        });
    }

    public void load() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
        }
        detailSource = repository.getDetail(idTransaksi);
        detail.addSource(detailSource, detail::setValue);
    }

    /** Count of delivered items of a rejected transaction (refund / re-order is offered when 0). */
    public void loadDiantar() {
        if (diantarSource != null) {
            diantar.removeSource(diantarSource);
        }
        diantarSource = repository.getJumlahItemDiantar(idTransaksi);
        diantar.addSource(diantarSource, diantar::setValue);
    }

    public void pesananDiterima(BaseRepository.ResultCallback<Boolean> callback) {
        repository.pesananDiterima(idTransaksi, callback);
    }

    public boolean isDeliverySet() {
        return delivery.current().isSet;
    }

    public void clearDelivery() {
        delivery.clear();
    }

    public void belanjaUlang(BaseRepository.ResultCallback<TransaksiResponses.BelanjaUlang> callback) {
        repository.belanjaUlang(idTransaksi, callback);
    }

    public void refreshCartCount() {
        cart.refreshCartCount();
    }
}
