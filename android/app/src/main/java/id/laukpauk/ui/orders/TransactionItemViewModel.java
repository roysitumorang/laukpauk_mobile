package id.laukpauk.ui.orders;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.GiftInfo;
import id.laukpauk.data.model.orders.TransactionGift;
import id.laukpauk.data.model.orders.TransactionItem;
import id.laukpauk.data.repository.TransactionRepository;

/** Lines and gifts of one transaction. */
public class TransactionItemViewModel extends ViewModel {

    /** A gift of the cart; {@code info} stays null until its details arrived. */
    public static final class GiftRow {
        public final String idGift;
        public GiftInfo info;

        GiftRow(String idGift) {
            this.idGift = idGift;
        }
    }

    private final TransactionRepository repository;
    private final String transactionId;

    private final MediatorLiveData<Resource<List<TransactionItem>>> items = new MediatorLiveData<>();
    private LiveData<Resource<List<TransactionItem>>> itemsSource;
    private final MediatorLiveData<Resource<List<TransactionGift>>> giftList = new MediatorLiveData<>();
    private LiveData<Resource<List<TransactionGift>>> giftSource;
    private final MutableLiveData<List<GiftRow>> gifts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> cartCodeMissing = new MutableLiveData<>(false);
    private int giftSeq;

    public TransactionItemViewModel(TransactionRepository repository, String transactionId) {
        this.repository = repository;
        this.transactionId = transactionId;
    }

    public LiveData<Resource<List<TransactionItem>>> getItems() {
        return items;
    }

    public LiveData<List<GiftRow>> getGifts() {
        return gifts;
    }

    public LiveData<Boolean> getCartCodeMissing() {
        return cartCodeMissing;
    }

    /** (Re)loads the lines and the gifts of {@code cartCode} (null = the cart code is unknown). */
    public void load(String cartCode) {
        if (itemsSource != null) {
            items.removeSource(itemsSource);
        }
        itemsSource = repository.getTransactionItem(transactionId);
        items.addSource(itemsSource, items::setValue);

        final int seq = ++giftSeq;
        gifts.setValue(new ArrayList<>());
        cartCodeMissing.setValue(cartCode == null);
        if (giftSource != null) {
            giftList.removeSource(giftSource);
            giftSource = null;
        }
        if (cartCode == null) {
            return;
        }
        giftSource = repository.getTransactionGift(cartCode);
        giftList.addSource(giftSource, res -> {
            if (seq != giftSeq || !res.isSuccess() || res.data == null) {
                return;
            }
            final List<GiftRow> rows = new ArrayList<>();
            for (TransactionGift g : res.data) {
                rows.add(new GiftRow(g.idGift));
            }
            gifts.setValue(rows);
            for (GiftRow row : rows) {
                repository.getGift(row.idGift, r -> {
                    if (seq != giftSeq || !r.isSuccess() || r.data == null) {
                        return; // legacy quirk: a missing gift keeps its spinner
                    }
                    row.info = r.data;
                    gifts.setValue(new ArrayList<>(rows));
                });
            }
        });
    }
}
