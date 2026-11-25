package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.Collections;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.TransactionItem;
import id.laukpauk.data.model.orders.Transaction;
import id.laukpauk.data.model.orders.TransactionResponses;
import id.laukpauk.data.remote.TransactionApi;

/** The buyer's transactions: history, detail, items, "pesanan diterima" and "belanja ulang". */
public class TransactionRepository extends BaseRepository {

    /** Status filter of each tab of Pesanan Saya (tab 0 = today, no status). */
    private static final String[] TAB_STATUS = {null, "pending", "processing", "otw", "received", "rejected"};

    private static final String ITEM_ENTITIES =
            "id,  namaProduk, namaSatuan, quantity, harga, diskon, catatan, jenisProduk, status, diskonPromo";

    private final TransactionApi api;
    private final DeliveryRepository delivery;

    public TransactionRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(TransactionApi.class);
        this.delivery = delivery;
    }

    /**
     * Legacy PesananSaya.getTransactionRecords. {@code tab} 0..5; {@code today} (tab 0 only) is the status
     * dropdown value ("" = all); {@code awal}/{@code akhir} ("yyyy-MM-d", tabs 1..5 only) the date range.
     */
    public LiveData<Resource<List<Transaction>>> getRecords(int tab, String today, String start, String end) {
        String status = (tab >= 0 && tab < TAB_STATUS.length) ? TAB_STATUS[tab] : null;
        String todayQ = (status == null && today != null && !today.isEmpty()) ? today : null;
        String startQ = (status != null && start != null && !start.isEmpty()) ? start : null;
        String endQ = (status != null && end != null && !end.isEmpty()) ? end : null;
        return load(api.getTransactionRecords(status, todayQ, startQ, endQ),
                body -> body == null || body.transactionRecords == null
                        ? Collections.<Transaction>emptyList() : body.transactionRecords);
    }

    /** Transaction detail; SUCCESS with null data when it does not exist. */
    public LiveData<Resource<Transaction>> getDetail(String transactionId) {
        return getDetail(transactionId, null);
    }

    /** {@code entitas} limits the returned columns (e.g. "transactionNumber, tanggalDelivery"). */
    public LiveData<Resource<Transaction>> getDetail(String transactionId, @Nullable String entities) {
        return load(api.getTransaction(transactionId, entities),
                body -> body == null ? null : body.transaction);
    }

    /** The lines of a transaction. */
    public LiveData<Resource<List<TransactionItem>>> getTransactionItem(String transactionId) {
        return load(api.getTransactionItem(transactionId, ITEM_ENTITIES),
                body -> body == null || body.transactionItem == null
                        ? Collections.<TransactionItem>emptyList() : body.transactionItem);
    }

    /** Number of items already delivered (a rejected transaction can be refunded/re-ordered when 0). */
    public LiveData<Resource<Integer>> getDeliveredItemCount(String transactionId) {
        return load(api.getDeliveredItemCount(transactionId),
                body -> body == null ? 0 : body.deliveredGoodsCount);
    }

    /** Marks an "otw" transaction as received; SUCCESS(true) when the server accepted it. */
    public void orderReceived(String transactionId, ResultCallback<Boolean> callback) {
        enqueue(api.orderReceived(transactionId), body -> body != null && body.orderReceived,
                callback);
    }

    /** Legacy TransaksiModel.reorder: re-adds a rejected transaction; the saved schedule is sent when one is chosen. */
    public void reorder(String transactionId, ResultCallback<TransactionResponses.Reorder> callback) {
        DeliverySelection selection = delivery.current();
        String code = selection.deliveryScheduleCode;
        String day = code == null ? null : selection.dayDelivery != null ? selection.dayDelivery : "Hari Pengantaran";
        enqueue(api.reorder(transactionId, body("dayDelivery", day, "jadwalDeliveryCode", code, "cartCode",
                session.getCartCode())), body -> body, callback);
    }
}
