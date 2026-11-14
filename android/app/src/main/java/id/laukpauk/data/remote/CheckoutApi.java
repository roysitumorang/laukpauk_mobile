package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.checkout.CheckoutResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Checkout endpoints (JSON; ApiClient adds the auth and version headers). */
public interface CheckoutApi {

    @GET("Auth/cekVerifikasiUser")
    Call<CheckoutResponses.Verified> cekVerifikasiUser();

    @GET("User/getDetailUser")
    Call<CheckoutResponses.DetailUserResponse> getDetailUser(@Query("getUserEntities") String select);

    @POST("JadwalDelivery/isJadwalDeliveryOpened")
    Call<CheckoutResponses.JadwalOpened> isJadwalDeliveryOpened(@Body Map<String, Object> body);

    @GET("User/getTotalKeranjang")
    Call<CheckoutResponses.TotalKeranjang> getTotalKeranjang(
            @Query("cartCode") String cartCode,
            @Query("latDelivery") String lat,
            @Query("longDelivery") String lng);

    @GET("User/getTotalOngkir")
    Call<CheckoutResponses.TotalOngkir> getTotalOngkir(@Query("cartCode") String cartCode,
                                                       @Query("latDelivery") String lat,
                                                       @Query("longDelivery") String lng);

    @GET("User/getJumlahCart")
    Call<CheckoutResponses.JumlahCart> getJumlahCart(@Query("cartCode") String cartCode);

    @GET("Transaksi/getJenisOngkir")
    Call<CheckoutResponses.JenisOngkir> getJenisOngkir(@Query("cartCode") String cartCode,
                                                       @Query("latDelivery") String lat,
                                                       @Query("longDelivery") String lng);

    @GET("User/getDiskonKategori")
    Call<CheckoutResponses.DiskonKategori> getDiskonKategori(@Query("cartCode") String cartCode);

    @GET("Pengaturan/getWalletConfiguration")
    Call<CheckoutResponses.WalletConfig> getWalletConfiguration();

    @GET("User/getTotalDompet")
    Call<CheckoutResponses.TotalDompet> getTotalDompet();

    @GET("Point/getPoint")
    Call<CheckoutResponses.PointResponse> getPoint(@Query("point") String point);

    @GET("User/getVoucher")
    Call<CheckoutResponses.VoucherList> getVoucher(@Query("entitasVoucher") String entitasVoucher,
                                                   @Query("forThisTotalBelanja") String totalBelanja);

    @GET("Voucher/getStatusVoucher/{id}")
    Call<CheckoutResponses.StatusVoucher> getStatusVoucher(@Path("id") String idVoucher);

    @GET("Promo/cekKodePromo/{kode}")
    Call<CheckoutResponses.KodePromo> cekKodePromo(@Path("kode") String kodePromo);
}
