package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.TransactionItem;
import id.laukpauk.data.repository.TransactionRepository;

/** Lines of one transaction. */
public class TransactionItemViewModel extends ViewModel {

    private final TransactionRepository repository;
    private final String transactionId;

    private final MediatorLiveData<Resource<List<TransactionItem>>> items = new MediatorLiveData<>();
    private LiveData<Resource<List<TransactionItem>>> itemsSource;

    public TransactionItemViewModel(TransactionRepository repository, String transactionId) {
        this.repository = repository;
        this.transactionId = transactionId;
    }

    public LiveData<Resource<List<TransactionItem>>> getItems() {
        return items;
    }

    /** (Re)loads the lines. */
    public void load() {
        if (itemsSource != null) {
            items.removeSource(itemsSource);
        }
        itemsSource = repository.getTransactionItem(transactionId);
        items.addSource(itemsSource, items::setValue);
    }
}
