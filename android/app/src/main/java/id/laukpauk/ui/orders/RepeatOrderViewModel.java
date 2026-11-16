package id.laukpauk.ui.orders;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.RepeatOrderResponses;
import id.laukpauk.data.model.orders.Transaction;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.CartRepository;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.data.repository.RepeatOrderRepository;
import id.laukpauk.data.repository.TransactionRepository;

/** State of Repeat Order: the old transaction's number and its re-orderable lines. */
public class RepeatOrderViewModel extends ViewModel {

    private final TransactionRepository transaction;
    private final RepeatOrderRepository repeatOrder;
    private final DeliveryRepository delivery;
    private final CartRepository cart;
    private final String transactionId;

    private final MediatorLiveData<Resource<Transaction>> detail = new MediatorLiveData<>();
    private LiveData<Resource<Transaction>> detailSource;
    private final MediatorLiveData<Resource<RepeatOrderResponses.Items>> items = new MediatorLiveData<>();
    private LiveData<Resource<RepeatOrderResponses.Items>> itemsSource;
    private final MutableLiveData<Boolean> processing = new MutableLiveData<>(false);
    private String itemsKey;

    public RepeatOrderViewModel(TransactionRepository transaction, RepeatOrderRepository repeatOrder,
                                DeliveryRepository delivery, CartRepository cart, String transactionId) {
        this.transaction = transaction;
        this.repeatOrder = repeatOrder;
        this.delivery = delivery;
        this.cart = cart;
        this.transactionId = transactionId;
        loadDetail();
    }

    public LiveData<Resource<Transaction>> getDetail() {
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
        detailSource = transaction.getDetail(transactionId, "nomorTransaksi, tanggalDelivery");
        detail.addSource(detailSource, detail::setValue);
    }

    /** Loads the lines for the current delivery selection; no-op when it is already loaded for it. */
    public void loadItemsIfNeeded(@Nullable DeliverySelection s) {
        String key = s == null || !s.isSet ? "unset" : s.latitude + "|" + s.longitude + "|" + s.deliveryScheduleCode;
        if (!key.equals(itemsKey)) {
            itemsKey = key;
            loadItems();
        }
    }

    private void loadItems() {
        if (itemsSource != null) {
            items.removeSource(itemsSource);
        }
        itemsSource = repeatOrder.getTransactionItem(transactionId);
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
        repeatOrder.addToCart(transactionId, result -> {
            if (result.isSuccess() && result.data != null && result.data.statusAddCart) {
                cart.refreshCartCount();
            }
            processing.setValue(false);
            callback.onResult(result);
        });
    }
}
