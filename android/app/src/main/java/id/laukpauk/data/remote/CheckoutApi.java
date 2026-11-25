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

    @POST("JadwalDelivery/isJadwalDeliveryOpened")
    Call<CheckoutResponses.ScheduleOpened> isDeliveryScheduleOpened(@Body Map<String, Object> body);

    @GET("User/getJumlahCart")
    Call<CheckoutResponses.CartQuantity> getCartQuantity(@Query("cartCode") String cartCode);

    @GET("User/getDiskonKategori")
    Call<CheckoutResponses.CategoryDiscount> getCategoryDiscount(@Query("cartCode") String cartCode);

    /**
     * The buyer's active vouchers whose {@code minimum_purchase} is at most {@code purchase} (buyer JWT required);
     * the vouchers are in {@code data}.
     */
    @GET("buyer/vouchers")
    Call<CheckoutResponses.VoucherList> buyerVouchers(@Query("purchase") long purchase);

    @GET("Promo/cekKodePromo/{kode}")
    Call<CheckoutResponses.PromoCode> checkPromoCode(@Path("kode") String promoCode);
}
