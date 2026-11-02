package id.laukpauk.data.remote;

import id.laukpauk.data.model.seller.shop.TokoSayaResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Seller sales history endpoints (versionCode is added by ApiClient). */
public interface HistoriPenjualanApi {

    @GET("Penjual/getHistoriPenjualan")
    Call<TokoSayaResponses.HistoriList> getHistoriPenjualan(@Query("selectTransaksi") String select,
                                                            @Query("statusTransaksi") String statusTransaksi,
                                                            @Query("authToken") String authToken);

    @GET("Penjual/getHistoriPenjualan/{idPenjual}")
    Call<TokoSayaResponses.HistoriList> getHistoriPenjualan(@Path("idPenjual") String idPenjual,
                                                            @Query("selectTransaksi") String select,
                                                            @Query("statusTransaksi") String statusTransaksi,
                                                            @Query("authToken") String authToken);

    @GET("Transaksi/getTotalTransaksiForThisPenjual/{idTransaksi}")
    Call<TokoSayaResponses.TotalTransaksi> getTotalTransaksi(@Path("idTransaksi") String idTransaksi,
                                                             @Query("authToken") String authToken);
}
