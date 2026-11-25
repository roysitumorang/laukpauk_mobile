package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.BuyerTransaction;
import id.laukpauk.data.model.orders.BuyerTransactionItem;
import id.laukpauk.data.repository.TransactionRepository;

/** Lines of one transaction: {@code items} of {@code GET buyer/transactions/:id}. */
public class TransactionItemViewModel extends ViewModel {

    private final TransactionRepository repository;
    private final String transactionId;

    private final MediatorLiveData<Resource<List<BuyerTransactionItem>>> items = new MediatorLiveData<>();
    private LiveData<Resource<BuyerTransaction>> source;

    public TransactionItemViewModel(TransactionRepository repository, String transactionId) {
        this.repository = repository;
        this.transactionId = transactionId;
    }

    public LiveData<Resource<List<BuyerTransactionItem>>> getItems() {
        return items;
    }

    /** (Re)loads the transaction and keeps its lines. */
    public void load() {
        if (source != null) {
            items.removeSource(source);
        }
        source = repository.getBuyerTransaction(transactionId);
        items.addSource(source, r -> {
            if (r.isLoading()) {
                items.setValue(Resource.loading());
            } else if (r.isSuccess()) {
                items.setValue(Resource.success(lines(r.data)));
            } else {
                items.setValue(Resource.error(r.message, r.code));
            }
        });
    }

    /** The lines of a transaction already loaded (the detail screen). */
    public void show(BuyerTransaction t) {
        if (source != null) {
            items.removeSource(source);
            source = null;
        }
        items.setValue(Resource.success(lines(t)));
    }

    static List<BuyerTransactionItem> lines(BuyerTransaction t) {
        List<BuyerTransactionItem> rows = new ArrayList<>();
        if (t != null && t.items != null) {
            for (BuyerTransactionItem item : t.items) {
                if (item != null) {
                    rows.add(item);
                }
            }
        }
        return rows;
    }
}
