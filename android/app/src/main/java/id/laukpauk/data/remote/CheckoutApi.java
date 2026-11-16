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
    Call<CheckoutResponses.Verified> checkUserVerification();

    @GET("User/getDetailUser")
    Call<CheckoutResponses.DetailUserResponse> getDetailUser(@Query("getUserEntities") String select);

    @POST("JadwalDelivery/isJadwalDeliveryOpened")
    Call<CheckoutResponses.ScheduleOpened> isDeliveryScheduleOpened(@Body Map<String, Object> body);

    @GET("User/getTotalKeranjang")
    Call<CheckoutResponses.CartTotal> getCartTotal(
            @Query("cartCode") String cartCode,
            @Query("latDelivery") String lat,
            @Query("longDelivery") String lng);

    @GET("User/getTotalOngkir")
    Call<CheckoutResponses.TotalShippingFee> getTotalShippingFee(@Query("cartCode") String cartCode,
                                                       @Query("latDelivery") String lat,
                                                       @Query("longDelivery") String lng);

    @GET("User/getJumlahCart")
    Call<CheckoutResponses.CartQuantity> getCartQuantity(@Query("cartCode") String cartCode);

    @GET("Transaksi/getJenisOngkir")
    Call<CheckoutResponses.ShippingFeeType> getShippingFeeType(@Query("cartCode") String cartCode,
                                                       @Query("latDelivery") String lat,
                                                       @Query("longDelivery") String lng);

    @GET("User/getDiskonKategori")
    Call<CheckoutResponses.CategoryDiscount> getCategoryDiscount(@Query("cartCode") String cartCode);

    @GET("Pengaturan/getWalletConfiguration")
    Call<CheckoutResponses.WalletConfig> getWalletConfiguration();

    @GET("User/getTotalDompet")
    Call<CheckoutResponses.WalletTotal> getWalletTotal();

    @GET("Point/getPoint")
    Call<CheckoutResponses.PointResponse> getPoint(@Query("point") String point);

    @GET("User/getVoucher")
    Call<CheckoutResponses.VoucherList> getVoucher(@Query("entitasVoucher") String voucherEntities,
                                                   @Query("forThisTotalBelanja") String shoppingTotal);

    @GET("Voucher/getStatusVoucher/{id}")
    Call<CheckoutResponses.StatusVoucher> getStatusVoucher(@Path("id") String idVoucher);

    @GET("Promo/cekKodePromo/{kode}")
    Call<CheckoutResponses.PromoCode> checkPromoCode(@Path("kode") String promoCode);
}
