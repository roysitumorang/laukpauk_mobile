package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.Transaction;
import id.laukpauk.data.model.orders.TransactionResponses;
import id.laukpauk.data.repository.BaseRepository;
import id.laukpauk.data.repository.CartRepository;
import id.laukpauk.data.repository.DeliveryRepository;
import id.laukpauk.data.repository.SettingRepository;
import id.laukpauk.data.repository.TransactionRepository;

/** State of Detail Transaksi: the transaction, the bank account (for BCA transfers) and delivered-item count. */
public class TransactionDetailViewModel extends ViewModel {

    private final TransactionRepository repository;
    private final DeliveryRepository delivery;
    private final SettingRepository settings;
    private final CartRepository cart;
    private final String transactionId;

    private final MediatorLiveData<Resource<Transaction>> detail = new MediatorLiveData<>();
    private LiveData<Resource<Transaction>> detailSource;
    private final MediatorLiveData<Resource<Integer>> delivered = new MediatorLiveData<>();
    private LiveData<Resource<Integer>> deliveredSource;
    /** {bankAccountNumber, bankAccountOwner}; null until the settings answered. */
    private final MutableLiveData<String[]> bankAccount = new MutableLiveData<>();

    public TransactionDetailViewModel(TransactionRepository repository, DeliveryRepository delivery,
                                    SettingRepository settings, CartRepository cart, String transactionId) {
        this.repository = repository;
        this.delivery = delivery;
        this.settings = settings;
        this.cart = cart;
        this.transactionId = transactionId;
        loadBankAccount();
        load();
    }

    public LiveData<Resource<Transaction>> getDetail() {
        return detail;
    }

    public LiveData<Resource<Integer>> getDelivered() {
        return delivered;
    }

    public LiveData<String[]> getBankAccount() {
        return bankAccount;
    }

    private void loadBankAccount() {
        final String[] values = {"", ""};
        settings.getSettingValue("nomorRekeningLP", r1 -> {
            values[0] = SettingRepository.asString(r1.data);
            settings.getSettingValue("pemilikNomorRekeningLP", r2 -> {
                values[1] = SettingRepository.asString(r2.data);
                bankAccount.setValue(values);
            });
        });
    }

    public void load() {
        if (detailSource != null) {
            detail.removeSource(detailSource);
        }
        detailSource = repository.getDetail(transactionId);
        detail.addSource(detailSource, detail::setValue);
    }

    /** Count of delivered items of a rejected transaction (refund / re-order is offered when 0). */
    public void loadDelivered() {
        if (deliveredSource != null) {
            delivered.removeSource(deliveredSource);
        }
        deliveredSource = repository.getDeliveredItemCount(transactionId);
        delivered.addSource(deliveredSource, delivered::setValue);
    }

    public void orderReceived(BaseRepository.ResultCallback<Boolean> callback) {
        repository.orderReceived(transactionId, callback);
    }

    public void reorder(BaseRepository.ResultCallback<TransactionResponses.Reorder> callback) {
        repository.reorder(transactionId, callback);
    }

    public void refreshCartCount() {
        cart.refreshCartCount();
    }
}
