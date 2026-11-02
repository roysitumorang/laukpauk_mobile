package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.RepeatOrderResponses;
import id.laukpauk.data.model.orders.RoPenjual;
import id.laukpauk.data.model.orders.Transaksi;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.CartRepository;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.data.repository.RepeatOrderRepository;
import id.laukpauk.data.repository.TransaksiRepository;

/** State of Repeat Order: the old transaction's number, the re-orderable sellers and the picked ones. */
public class RepeatOrderViewModel extends ViewModel {

    private final TransaksiRepository transaksi;
    private final RepeatOrderRepository repeatOrder;
    private final DeliveryRepository delivery;
    private final CartRepository cart;
    private final String idTransaksi;

    private final MediatorLiveData<Resource<Transaksi>> detail = new MediatorLiveData<>();
    private LiveData<Resource<Transaksi>> detailSource;
    private final MediatorLiveData<Resource<List<RoPenjual>>> items = new MediatorLiveData<>();
    private LiveData<Resource<List<RoPenjual>>> itemsSource;
    private final Set<String> selected = new LinkedHashSet<>();
    private final MutableLiveData<Integer> selectedCount = new MutableLiveData<>(0);
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

    public LiveData<Resource<List<RoPenjual>>> getItems() {
        return items;
    }

    public LiveData<DeliverySelection> getSelection() {
        return delivery.selection();
    }

    public Set<String> getSelected() {
        return selected;
    }

    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
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

    /** Loads the sellers for the current delivery selection; no-op when it is already loaded for it. */
    public void loadItemsIfNeeded(DeliverySelection s) {
        String key = s.latitude + "|" + s.longitude + "|" + s.jadwalDeliveryCode;
        if (!key.equals(itemsKey)) {
            itemsKey = key;
            loadItems();
        }
    }

    private void loadItems() {
        selected.clear();
        selectedCount.setValue(0);
        if (itemsSource != null) {
            items.removeSource(itemsSource);
        }
        itemsSource = repeatOrder.getItemTransaksiPerPenjual(idTransaksi);
        items.addSource(itemsSource, items::setValue);
    }

    /** Pull-to-refresh (legacy onRefresh): reload the number and a fresh list without a selection. */
    public void refresh() {
        loadDetail();
        loadItems();
    }

    public void toggle(String idPenjual) {
        if (!selected.remove(idPenjual)) {
            selected.add(idPenjual);
        }
        selectedCount.setValue(selected.size());
    }

    public void addToCart(BaseRepository.ResultCallback<RepeatOrderResponses.AddToCart> callback) {
        if (Boolean.TRUE.equals(processing.getValue())) {
            return;
        }
        processing.setValue(true);
        repeatOrder.addToCartThesePenjual(idTransaksi, new ArrayList<>(selected), result -> {
            if (result.isSuccess() && result.data != null && result.data.statusAddCart) {
                cart.refreshCartCount();
            }
            processing.setValue(false);
            callback.onResult(result);
        });
    }
}
