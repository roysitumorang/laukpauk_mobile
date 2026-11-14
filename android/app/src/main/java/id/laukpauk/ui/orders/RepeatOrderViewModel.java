package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.RepeatOrderResponses;
import id.laukpauk.data.model.orders.Transaksi;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.CartRepository;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.data.repository.RepeatOrderRepository;
import id.laukpauk.data.repository.TransaksiRepository;

/** State of Repeat Order: the old transaction's number and its re-orderable lines. */
public class RepeatOrderViewModel extends ViewModel {

    private final TransaksiRepository transaksi;
    private final RepeatOrderRepository repeatOrder;
    private final DeliveryRepository delivery;
    private final CartRepository cart;
    private final String idTransaksi;

    private final MediatorLiveData<Resource<Transaksi>> detail = new MediatorLiveData<>();
    private LiveData<Resource<Transaksi>> detailSource;
    private final MediatorLiveData<Resource<RepeatOrderResponses.Items>> items = new MediatorLiveData<>();
    private LiveData<Resource<RepeatOrderResponses.Items>> itemsSource;
    private final MutableLiveData<Boolean> processing = new MutableLiveData<>(false);
    private String itemsKey;

    public RepeatOrderViewModel(TransaksiRepository transaksi, RepeatOrderRepository repeatOrder,
                                DeliveryRepository delivery, CartRepository cart, String idTransaksi) {
        this.transaksi = transaksi;
        this.repeatOrder = repeatOrder;
        this.delivery = delivery;
        this.cart = cart;
        this.idTransaksi = idTransaksi;
        loadDetail();
    }

    public LiveData<Resource<Transaksi>> getDetail() {
        return detail;
    }

    public LiveData<Resource<RepeatOrderResponses.Items>> getItems() {
        return items;
    }

    public LiveData<DeliverySelection> getSelection() {
        return delivery.selection();
    }

    public LiveData<Boolean> getProcessing() {
        return processing;
    }

    private void loadDetail() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
        }
        detailSource = transaksi.getDetail(idTransaksi, "nomorTransaksi, tanggalDelivery");
        detail.addSource(detailSource, detail::setValue);
    }

    /** Loads the lines for the current delivery selection; no-op when it is already loaded for it. */
    public void loadItemsIfNeeded(DeliverySelection s) {
        String key = s.latitude + "|" + s.longitude + "|" + s.jadwalDeliveryCode;
        if (!key.equals(itemsKey)) {
            itemsKey = key;
            loadItems();
        }
    }

    private void loadItems() {
        if (itemsSource != null) {
            items.removeSource(itemsSource);
        }
        itemsSource = repeatOrder.getItemTransaksi(idTransaksi);
        items.addSource(itemsSource, items::setValue);
    }

    /** Pull-to-refresh (legacy onRefresh): reload the number and the lines. */
    public void refresh() {
        loadDetail();
        loadItems();
    }

    public void addToCart(BaseRepository.ResultCallback<RepeatOrderResponses.AddToCart> callback) {
        if (Boolean.TRUE.equals(processing.getValue())) {
            return;
        }
        processing.setValue(true);
        repeatOrder.addToCart(idTransaksi, result -> {
            if (result.isSuccess() && result.data != null && result.data.statusAddCart) {
                cart.refreshCartCount();
            }
            processing.setValue(false);
            callback.onResult(result);
        });
    }
}
