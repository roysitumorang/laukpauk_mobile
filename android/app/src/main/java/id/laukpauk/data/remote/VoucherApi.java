package id.laukpauk.data.remote;

import id.laukpauk.data.model.voucher.VoucherResponses;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Voucher endpoints (JSON; ApiClient adds the auth and version headers). */
public interface VoucherApi {

    @GET("User/getVoucher")
    Call<VoucherResponses.VoucherList> getVoucher(@Query("entitasVoucher") String entitasVoucher);

    @GET("Voucher/getVoucher/{idVoucher}")
    Call<VoucherResponses.Detail> getVoucherDetail(@Path("idVoucher") String idVoucher);
}
