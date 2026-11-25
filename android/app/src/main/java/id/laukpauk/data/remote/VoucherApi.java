package id.laukpauk.data.remote;

import androidx.annotation.Nullable;

import id.laukpauk.data.model.voucher.VoucherResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** The buyer's vouchers (buyer JWT required; ApiClient adds the auth and version headers). */
public interface VoucherApi {

    /**
     * The buyer's active vouchers; with {@code purchase} (the cart grand total), only those whose
     * {@code minimum_purchase} is at most it. The vouchers are in {@code data}.
     */
    @GET("buyer/vouchers")
    Call<VoucherResponses.BuyerVouchers> buyerVouchers(@Nullable @Query("purchase") Long purchase);
}
