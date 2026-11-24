package id.laukpauk.data.remote;

import java.util.Map;

import id.laukpauk.data.model.account.AccountResponses;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** The signed-in user: profile, wallet, vouchers, points (JSON; ApiClient adds the auth and version headers). */
public interface UserApi {

    /** The signed-in buyer (buyer JWT), including the {@code wallet} balance. */
    @GET("buyer/users/me")
    Call<AccountResponses.Me> me();

    @GET("User/getJumlahVoucher")
    Call<AccountResponses.VoucherCount> getVoucherCount();

    /** {@code walletType}: "referral", "potongantransaksi" or null for all. */
    @GET("User/getRiwayatDompet")
    Call<AccountResponses.WalletRecordList> getWalletRecords(@Query("jenisDompet") String walletType);

    @GET("User/getRiwayatPoint")
    Call<AccountResponses.PointRecordList> getPointRecords();

    @POST("User/updateProfil")
    Call<AccountResponses.UpdateProfile> updateProfile(@Body Map<String, Object> body);

    @POST("User/updateFotoProfil")
    Call<AccountResponses.UpdatePhoto> updateProfilePhoto(@Body Map<String, Object> body);

    @POST("User/gantiPassword")
    Call<AccountResponses.ChangePassword> changePassword(@Body Map<String, Object> body);
}
