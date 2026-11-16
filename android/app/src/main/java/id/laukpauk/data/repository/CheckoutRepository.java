package id.laukpauk.data.repository;

import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;


import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.checkout.CheckoutInit;
import id.laukpauk.data.model.checkout.CheckoutResponses;
import id.laukpauk.data.model.checkout.ShippingSchedule;
import id.laukpauk.data.model.checkout.Voucher;
import id.laukpauk.data.model.checkout.WalletConfiguration;
import id.laukpauk.data.remote.CheckoutApi;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;
import retrofit2.Call;

/**
 * Checkout (legacy CheckoutModel, KodePromoModel, PointModel / ProfilSayaModel / SettingModel as the checkout
 * uses them, LocalStorage.isUserVerified / getStatusVoucher). The cart is read through the cart code and the
 * saved delivery coordinates; the cart itself is edited by CartRepository.
 */
public class CheckoutRepository extends BaseRepository {

    /** {@link #checkPrerequisites}: the account is not verified yet (OTP verification screen). */
    public static final int STEP_NOT_VERIFIED = 1;
    /** The OTP registration was not completed. */
    public static final int STEP_OTP_NOT_COMPLETED = 2;
    /** The saved delivery schedule is already closed. */
    public static final int STEP_SCHEDULE_CLOSED = 3;
    /** All good: load the checkout data. */
    public static final int STEP_OK = 4;

    private final CheckoutApi api;
    private final SettingRepository settings;

    public CheckoutRepository(RepositoryDeps deps, SettingRepository settings) {
        super(deps);
        this.api = api(CheckoutApi.class);
        this.settings = settings;
    }

    private interface Next<R> {
        void run(R value);
    }

    /** Counts down finished requests; runs {@code done} after the last one. */
    private static final class Join {
        private int left;
        private final Runnable done;

        Join(int count, Runnable done) {
            this.left = count;
            this.done = done;
        }

        void step() {
            if (--left == 0) {
                done.run();
            }
        }
    }

    /** enqueue() that always continues: {@code fallback} replaces a failed or empty answer. */
    private <T, R> void call(Call<T> call, Mapper<T, R> mapper, R fallback, Next<R> next) {
        enqueue(call, mapper, (Resource<R> res) -> next.run(res.isSuccess() && res.data != null ? res.data : fallback));
    }

    private static String nz(@Nullable String s) {
        return s == null ? "" : s;
    }

    /** The saved delivery schedule, or null (legacy LocalStorage.savedDeliveryScheduleMap). */
    @Nullable
    public ShippingSchedule savedSchedule() {
        Map<String, Object> o = session.savedDeliveryScheduleMap();
        return o == null ? null : deps.apiClient.fromTree(o, ShippingSchedule.class);
    }

    private String savedLatitude() {
        return nz(Json.strOrNull(session.getSavedLatitudeAndLongitude(), "latitude"));
    }

    private String savedLongitude() {
        return nz(Json.strOrNull(session.getSavedLatitudeAndLongitude(), "longitude"));
    }

    public String cartCode() {
        return session.getCartCode();
    }

    /** Latitude of the saved delivery address ("" when none). */
    public String latDelivery() {
        return savedLatitude();
    }

    public String longDelivery() {
        return savedLongitude();
    }

    /**
     * Legacy _initializeState steps after sign-in: verified user, OTP completed, schedule still open.
     * Result is one of the STEP_ constants.
     */
    public void checkPrerequisites(ResultCallback<Integer> callback) {
        this.<CheckoutResponses.Verified, Boolean>call(api.checkUserVerification(),
                b -> b.isVerified, Boolean.FALSE, verified -> {
                    if (!verified) {
                        callback.onResult(Resource.success(STEP_NOT_VERIFIED));
                        return;
                    }
                    this.<CheckoutResponses.DetailUserResponse, Boolean>call(api.getDetailUser("statusOTP"),
                            b -> b.detailUser != null && "1".equals(b.detailUser.statusOTP), Boolean.FALSE, otp -> {
                                if (!otp) {
                                    callback.onResult(Resource.success(STEP_OTP_NOT_COMPLETED));
                                    return;
                                }
                                checkScheduleOpened(callback);
                            });
                });
    }

    private void checkScheduleOpened(ResultCallback<Integer> callback) {
        ShippingSchedule j = savedSchedule();
        if (j == null) {
            callback.onResult(Resource.success(STEP_OK));
            return;
        }
        this.<CheckoutResponses.ScheduleOpened, Boolean>call(
                api.isDeliveryScheduleOpened(body("kode", nz(j.code), "day", nz(j.day), "setOn", nz(j.setOn))),
                b -> b.isDeliveryScheduleOpened, Boolean.TRUE,
                open -> callback.onResult(Resource.success(open ? STEP_OK : STEP_SCHEDULE_CLOSED)));
    }

    /** Legacy _dataInitialization: totals, shipping type, wallet, points, buyer details. Always SUCCESS. */
    public void loadInit(ResultCallback<CheckoutInit> callback) {
        final CheckoutInit d = new CheckoutInit();
        d.cartCode = session.getCartCode();
        final String lat = savedLatitude();
        final String lng = savedLongitude();
        this.<CheckoutResponses.CartTotal, Long>call(api.getCartTotal(d.cartCode, lat, lng),
                b -> b.cartTotal, 0L, total -> {
                    d.shoppingTotal = total;
                    if (total < 1) {
                        callback.onResult(Resource.success(d));
                        return;
                    }
                    d.enough = true;
                    loadRest(d, lat, lng, callback);
                });
    }

    private void loadRest(final CheckoutInit d, String lat, String lng, ResultCallback<CheckoutInit> callback) {
        final Join all = new Join(6, () -> callback.onResult(Resource.success(d)));

        this.<CheckoutResponses.WalletConfig, WalletConfiguration>call(api.getWalletConfiguration(),
                b -> b.walletConfiguration, null, w -> {
                    d.walletConfiguration = w;
                    all.step();
                });
        this.<CheckoutResponses.TotalShippingFee, Long>call(api.getTotalShippingFee(d.cartCode, lat, lng),
                b -> b.totalShippingFee, 0L, v -> {
                    d.totalShippingFee = v;
                    all.step();
                });
        this.<CheckoutResponses.CartQuantity, Long>call(api.getCartQuantity(d.cartCode),
                b -> b.cartCount, 0L, v -> {
                    d.goodsCount = v;
                    all.step();
                });
        this.<CheckoutResponses.CategoryDiscount, Map<String, Object>>call(api.getCategoryDiscount(d.cartCode),
                b -> Json.isObject(b.categoryDiscount) ? Json.obj(b.categoryDiscount) : null, null, v -> {
                    d.categoryDiscount = v;
                    all.step();
                });
        this.<CheckoutResponses.WalletTotal, Long>call(api.getWalletTotal(),
                b -> b.walletTotal, 0L, v -> {
                    d.myWalletTotal = v;
                    all.step();
                });
        // 6th: the buyer's details, then the shipping explanation (which needs the shipping type)
        this.<CheckoutResponses.DetailUserResponse, CheckoutResponses.DetailUser>call(
                api.getDetailUser("nama, nomorHP, alamat"),
                b -> b.detailUser, null, user -> {
                    if (user != null) {
                        d.name = nz(user.name);
                        d.phoneNumber = nz(user.phoneNumber);
                        d.address = nz(user.address);
                    }
                    loadShipping(d, lat, lng, all);
                });
    }

    private void loadShipping(final CheckoutInit d, String lat, String lng, final Join all) {
        this.<CheckoutResponses.ShippingFeeType, CheckoutResponses.ShippingFeeType>call(
                api.getShippingFeeType(d.cartCode, lat, lng), b -> b, null, type -> {
                    if (type == null) {
                        all.step(); // legacy: the page never shows, the loading view stays
                        return;
                    }
                    d.shippingFeeType = type;
                    final boolean needsCost = "perkm".equals(type.shippingFeeType);
                    final Join inner = new Join(needsCost ? 3 : 2, () -> {
                        d.complete = true;
                        all.step();
                    });
                    if (needsCost) {
                        ResultCallback<Object> onCost = res -> {
                            long rate = res.isSuccess() ? Json.asLong(res.data, 0) : 0;
                            d.shippingFeeExplanation = explanation(type, rate);
                            inner.step();
                        };
                        settings.getSettingValue("costPerKm", onCost);
                    }
                    this.<CheckoutResponses.PointResponse, Long>call(api.getPoint("true"),
                            b -> b.response == null ? 0L : b.response.point, 0L, v -> {
                                d.point = v;
                                inner.step();
                            });
                    ResultCallback<Object> onMinimal = res -> {
                        if (res.isSuccess() && res.data != null) {
                            d.minimumPointRedeem = Json.asLong(res.data, 0);
                        }
                        inner.step();
                    };
                    settings.getSettingValue("minimalPointTukarPoint", onMinimal);
                });
    }

    private static String explanation(CheckoutResponses.ShippingFeeType type, long rate) {
        return "Anda dikenakan ongkir PERKM karena alamat pengantaran berada di luar "
                + "Coverage Area anda. Jarak yang harus ditempuh kurir adalah "
                + Currency.formatUnit((long) type.distance, "KM")
                + ". Tarif ongkir per km adalah " + Currency.formatRupiah(rate, 0);
    }

    /** Active vouchers usable for this basket total (empty list when none or on failure). */
    public LiveData<Resource<List<Voucher>>> getVouchers(long shoppingTotal) {
        return load(api.getVoucher("id, nama, foto, minimumBelanja, nilai, jenis, kode, maximumUsage",
                String.valueOf(shoppingTotal)),
                body -> body == null || body.listVoucher == null ? new ArrayList<Voucher>() : body.listVoucher);
    }

    /**
     * Voucher chosen: fetches its status ("aktif", ... or "kadaluarsa" on failure), remembers it as the used
     * voucher and reports the status (legacy getStatusVoucher + setUsedVoucher).
     */
    public void selectVoucher(final String idVoucher, final ResultCallback<String> callback) {
        this.<CheckoutResponses.StatusVoucher, String>call(api.getStatusVoucher(idVoucher),
                b -> b.statusVoucher, "kadaluarsa", status -> {
                    session.setUsedVoucher(idVoucher);
                    callback.onResult(Resource.success(status));
                });
    }

    public void clearUsedVoucher() {
        session.clearUsedVoucher();
    }

    /** Legacy KodePromoModel.checkPromoCode; ERROR when the request failed. */
    public void checkPromoCode(String promoCode, ResultCallback<CheckoutResponses.PromoCode> callback) {
        enqueue(api.checkPromoCode(promoCode), body -> body, callback);
    }
}
