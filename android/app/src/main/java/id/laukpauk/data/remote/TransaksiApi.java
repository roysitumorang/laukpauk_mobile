package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.orders.TransaksiResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Buyer transaction endpoints (JSON; ApiClient adds the auth and version headers). */
public interface TransaksiApi {

    @GET("User/getRiwayatTransaksi")
    Call<TransaksiResponses.RiwayatList> getRiwayatTransaksi(
            @Query("statusTransaksi") String statusTransaksi,
            @Query("statusTransaksiHariIni") String statusTransaksiHariIni,
            @Query("rentangAwal") String rentangAwal,
            @Query("rentangAkhir") String rentangAkhir);

    @GET("Transaksi/getTransaksi/{id}")
    Call<TransaksiResponses.Detail> getTransaksi(@Path("id") String id,
                                                 @Query("entitasTransaksi") String entitasTransaksi);

    @GET("Transaksi/getItemTransaksiFromTransaksi/{id}")
    Call<TransaksiResponses.Items> getItemTransaksi(@Path("id") String id,
                                                    @Query("entitasItemTransaksi") String entitasItemTransaksi);

    @GET("Transaksi/getJumlahItemDiantar/{id}")
    Call<TransaksiResponses.Diantar> getJumlahItemDiantar(@Path("id") String id);

    /** POST without a body. */
    @POST("Transaksi/pesananDiterima/{id}")
    Call<TransaksiResponses.PesananDiterima> pesananDiterima(@Path("id") String id);

    @POST("Transaksi/belanjaUlang/{id}")
    Call<TransaksiResponses.BelanjaUlang> belanjaUlang(@Path("id") String id, @Body Map<String, Object> body);

    @GET("gift/gift-transaksi")
    Call<TransaksiResponses.GiftList> getGiftTransaksi(@Query("cartCode") String cartCode);

    @GET("Gift/getGift/{idGift}")
    Call<TransaksiResponses.GiftDetail> getGift(@Path("idGift") String idGift);
}
