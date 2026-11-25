package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.orders.TransactionResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Buyer transaction endpoints (JSON; ApiClient adds the auth and version headers). */
public interface TransactionApi {

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
