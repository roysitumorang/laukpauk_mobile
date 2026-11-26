package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.checkout.CheckoutResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import id.laukpauk.data.model.promotion.BuyerPromotionResponses;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Checkout endpoints (JSON; ApiClient adds the auth and version headers). */
public interface CheckoutApi {

    @POST("JadwalDelivery/isJadwalDeliveryOpened")
    Call<CheckoutResponses.ScheduleOpened> isDeliveryScheduleOpened(@Body Map<String, Object> body);

    @GET("User/getDiskonKategori")
    Call<CheckoutResponses.CategoryDiscount> getCategoryDiscount(@Query("cartCode") String cartCode);

    /** The promotion with this code while the buyer can use it; 404 when the code is not valid for them. */
    @GET("buyer/promotions/{promo_code}")
    Call<BuyerPromotionResponses.One> buyerPromotion(@Path("promo_code") String promoCode);
}
