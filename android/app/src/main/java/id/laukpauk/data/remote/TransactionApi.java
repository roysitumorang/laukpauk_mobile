package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.orders.TransactionResponses;
import androidx.annotation.Nullable;

import id.laukpauk.data.model.orders.BuyerTransactionResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/** Buyer transaction endpoints (JSON; ApiClient adds the auth and version headers). */
public interface TransactionApi {

    /**
     * The buyer's transactions, newest first; page-based ({@code data.links.next}). {@code status} 0..8 (null = all),
     * {@code created_at_start} / {@code created_at_end} RFC 3339 (null = no bound).
     */
    @GET("buyer/transactions")
    Call<BuyerTransactionResponses.Page> buyerTransactions(@Query("limit") int limit,
                                                           @Nullable @Query("status") Integer status,
                                                           @Nullable @Query("created_at_start") String createdAtStart,
                                                           @Nullable @Query("created_at_end") String createdAtEnd);

    /** A next page: {@code data.links.next}, a full URL. */
    @GET
    Call<BuyerTransactionResponses.Page> buyerTransactionsPage(@Url String next);

    /** One of the buyer's transactions with its items; 404 when it is not the buyer's. */
    @GET("buyer/transactions/{id}")
    Call<BuyerTransactionResponses.One> buyerTransaction(@Path("id") String id);

    @GET("User/getRiwayatTransaksi")
    Call<TransactionResponses.RecordsList> getTransactionRecords(
            @Query("statusTransaksi") String transactionStatus,
            @Query("statusTransaksiHariIni") String todayTransactionStatus,
            @Query("rentangAwal") String rangeStart,
            @Query("rentangAkhir") String rangeEnd);

    @GET("Transaksi/getTransaksi/{id}")
    Call<TransactionResponses.Detail> getTransaction(@Path("id") String id,
                                                 @Query("entitasTransaksi") String transactionEntities);

    @GET("Transaksi/getItemTransaksiFromTransaksi/{id}")
    Call<TransactionResponses.Items> getTransactionItem(@Path("id") String id,
                                                    @Query("entitasItemTransaksi") String transactionItemEntities);

    @GET("Transaksi/getJumlahItemDiantar/{id}")
    Call<TransactionResponses.Delivered> getDeliveredItemCount(@Path("id") String id);

    /** POST without a body. */
    @POST("Transaksi/pesananDiterima/{id}")
    Call<TransactionResponses.OrderReceived> orderReceived(@Path("id") String id);

    @POST("Transaksi/belanjaUlang/{id}")
    Call<TransactionResponses.Reorder> reorder(@Path("id") String id, @Body Map<String, Object> body);
}
