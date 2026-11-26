package id.laukpauk.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.orders.BuyerTransactionResponses;
import id.laukpauk.data.model.orders.BuyerTransaction;
import id.laukpauk.data.Paged;
import id.laukpauk.data.model.delivery.DeliverySelection;
import id.laukpauk.data.model.orders.Transaction;
import id.laukpauk.data.model.orders.TransactionResponses;
import id.laukpauk.data.remote.TransactionApi;

/**
 * The buyer's transactions: {@code GET buyer/transactions} (list, detail), and the legacy detail (belanja lagi),
 * "pesanan diterima" and "belanja ulang".
 */
public class TransactionRepository extends BaseRepository {

    private final TransactionApi api;
    private final DeliveryRepository delivery;

    public TransactionRepository(RepositoryDeps deps, DeliveryRepository delivery) {
        super(deps);
        this.api = api(TransactionApi.class);
        this.delivery = delivery;
    }

    /** Rows per page of {@code GET buyer/transactions}. */
    public static final int PAGE_LIMIT = 25;

    /**
     * {@code GET buyer/transactions}: the first page. {@code status} 0..8 or null (all); {@code start} / {@code end}
     * the creation dates (RFC 3339) or null.
     */
    public void getBuyerTransactions(@Nullable Integer status, @Nullable String start, @Nullable String end,
                                     ResultCallback<Paged<BuyerTransaction>> callback) {
        enqueue(api.buyerTransactions(PAGE_LIMIT, status, start, end), TransactionRepository::page, callback);
    }

    /** The page at {@code next} ({@code data.links.next} of the previous page). */
    public void getBuyerTransactionsPage(String next, ResultCallback<Paged<BuyerTransaction>> callback) {
        enqueue(api.buyerTransactionsPage(next), TransactionRepository::page, callback);
    }

    /** Rows and next-page URL of a {@code GET buyer/transactions} page. */
    public static Paged<BuyerTransaction> page(@Nullable BuyerTransactionResponses.Page body) {
        BuyerTransactionResponses.PageData data = body == null ? null : body.data;
        if (data == null) {
            return Paged.empty();
        }
        List<BuyerTransaction> rows = new ArrayList<>();
        if (data.rows != null) {
            for (BuyerTransaction t : data.rows) {
                if (t != null) {
                    rows.add(t);
                }
            }
        }
        return new Paged<>(rows, data.links == null ? null : data.links.next);
    }

    /** {@code GET buyer/transactions/:id}: the transaction with its items; ERROR 404 when it is not there. */
    public LiveData<Resource<BuyerTransaction>> getBuyerTransaction(String transactionId) {
        return load(api.buyerTransaction(transactionId),
                (BuyerTransactionResponses.One body) -> body == null ? null : body.data);
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
