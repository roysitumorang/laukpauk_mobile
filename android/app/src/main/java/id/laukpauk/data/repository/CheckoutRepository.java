package id.laukpauk.data.repository;

import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;


import java.util.ArrayList;
import java.util.List;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.account.AccountResponses;
import id.laukpauk.data.model.account.UserDetail;
import id.laukpauk.data.model.address.BuyerAddress;
import id.laukpauk.data.model.cart.CartResponses;
import id.laukpauk.data.model.checkout.CheckoutInit;
import id.laukpauk.data.model.checkout.CheckoutResponses;
import id.laukpauk.data.model.checkout.ShippingSchedule;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.data.model.setting.BuyerSetting;
import id.laukpauk.data.remote.CartApi;
import id.laukpauk.data.remote.CheckoutApi;
import id.laukpauk.data.remote.VoucherApi;
import id.laukpauk.data.remote.UserApi;
import id.laukpauk.util.Currency;
import id.laukpauk.util.Json;
import retrofit2.Call;

/**
 * Checkout (legacy CheckoutModel, KodePromoModel, PointModel / ProfilSayaModel / SettingModel as the checkout
 * uses them, LocalStorage.isUserVerified). The cart is read through the cart code and the
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
    private final CartApi cartApi;
    private final UserApi userApi;
    private final SettingRepository settings;
    private final AddressRepository addresses;
    private final VoucherApi vouchers;

    public CheckoutRepository(RepositoryDeps deps, SettingRepository settings, AddressRepository addresses) {
        super(deps);
        this.api = api(CheckoutApi.class);
        this.cartApi = api(CartApi.class);
        this.userApi = api(UserApi.class);
        this.settings = settings;
        this.addresses = addresses;
        this.vouchers = api(VoucherApi.class);
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
     * Steps before checkout: the buyer's mobile phone is confirmed ({@code mobile_phone_confirmed_at} of the
     * signed-in user, set by {@code POST buyer/users/mobile_phone/confirm}), then the schedule is still open.
     * Result is one of the STEP_ constants.
     */
    public void checkPrerequisites(ResultCallback<Integer> callback) {
        if (!session.isMobilePhoneConfirmed()) {
            callback.onResult(Resource.success(STEP_NOT_VERIFIED));
            return;
        }
        checkScheduleOpened(callback);
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

    /** Checkout goes on only for a grand total above 0 (validated in the app; the backend has no minimum). */
    public static boolean hasPositiveTotal(long grandTotal) {
        return grandTotal > 0;
    }

    /** Legacy _dataInitialization: totals, delivery cost, wallet, points, buyer details. Always SUCCESS. */
    public void loadInit(ResultCallback<CheckoutInit> callback) {
        final CheckoutInit d = new CheckoutInit();
        d.cartCode = session.getCartCode();
        final String lat = savedLatitude();
        final String lng = savedLongitude();
        // the shopping total is the buyer's cart (GET buyer/cart): Σ unit price × quantity; checked in the app
        this.<CartResponses.BuyerCartEnvelope, Long>call(cartApi.buyerCart(),
                b -> b.data == null ? 0L : b.data.total(), 0L, total -> {
                    d.shoppingTotal = total;
                    if (!hasPositiveTotal(total)) {
                        callback.onResult(Resource.success(d));
                        return;
                    }
                    d.enough = true;
                    loadRest(d, lat, lng, callback);
                });
    }

    private void loadRest(final CheckoutInit d, String lat, String lng, ResultCallback<CheckoutInit> callback) {
        final Join all = new Join(4, () -> callback.onResult(Resource.success(d)));

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
        // the buyer (GET buyer/users/me): name, phone, wallet and points
        this.<AccountResponses.Me, UserDetail>call(userApi.me(), UserRepository::me, null, user -> {
            if (user != null) {
                d.name = nz(user.name);
                d.phoneNumber = nz(user.phoneNumber);
                d.myWalletTotal = user.walletOrZero();
                d.point = user.rewardPointsOrZero();
            }
            all.step();
        });
        loadShipping(d, lat, lng, all);
    }

    /**
     * The delivery cost: {@code distance_km} of the delivery address ({@code GET buyer/delivery_addresses}, the
     * address at the saved point) times {@code delivery_cost_per_km} of {@code GET buyer/setting}. Without both,
     * {@code complete} stays false.
     */
    private void loadShipping(final CheckoutInit d, String lat, String lng, final Join all) {
        final BuyerAddress[] address = {null};
        final BuyerSetting[] setting = {null};
        final Join inner = new Join(3, () -> {
            if (address[0] != null) {
                // "Alamat Lengkap Penerima": street_address of the chosen delivery address
                d.address = nz(address[0].streetAddress);
            }
            if (address[0] != null && setting[0] != null) {
                d.distanceKm = address[0].distanceKm;
                d.deliveryCostPerKm = setting[0].deliveryCostPerKm;
                d.voucherEnabled = setting[0].voucherEnabled;
                d.totalShippingFee = deliveryCost(d.distanceKm, d.deliveryCostPerKm);
                d.shippingFeeExplanation = explanation(d.distanceKm, d.deliveryCostPerKm);
                d.complete = true;
            }
            all.step();
        });
        addresses.list(r -> {
            address[0] = r.isSuccess() ? AddressRepository.at(r.data, lat, lng) : null;
            inner.step();
        });
        settings.getBuyerSetting(r -> {
            setting[0] = r.isSuccess() ? r.data : null;
            inner.step();
        });
        settings.getSettingValue("minimalPointTukarPoint", res -> {
            if (res.isSuccess() && res.data != null) {
                d.minimumPointRedeem = Json.asLong(res.data, 0);
            }
            inner.step();
        });
    }

    /** Delivery cost in rupiah: the distance (whole km) times the cost per km. */
    public static long deliveryCost(long distanceKm, long costPerKm) {
        return Math.max(0, distanceKm) * Math.max(0, costPerKm);
    }

    static String explanation(long distanceKm, long costPerKm) {
        return "Ongkir dihitung dari jarak pengantaran " + Currency.formatUnit(distanceKm, "KM")
                + " dengan tarif " + Currency.formatRupiah(costPerKm, 0) + " per km.";
    }

    /**
     * The buyer's vouchers usable for this cart grand total ({@code GET buyer/vouchers?purchase=...}): active, with
     * {@code minimum_purchase} at most the total. Empty list when none (also on 404).
     */
    public LiveData<Resource<List<Voucher>>> getVouchers(long cartGrandTotal) {
        return load(vouchers.buyerVouchers(Math.max(0, cartGrandTotal)), VoucherRepository::rows);
    }

    /** Voucher chosen: remembers it as the used voucher. */
    public void selectVoucher(Voucher voucher) {
        session.setUsedVoucher(voucher.id);
    }

    public void clearUsedVoucher() {
        session.clearUsedVoucher();
    }

    /** Legacy KodePromoModel.checkPromoCode; ERROR when the request failed. */
    public void checkPromoCode(String promoCode, ResultCallback<CheckoutResponses.PromoCode> callback) {
        enqueue(api.checkPromoCode(promoCode), body -> body, callback);
    }
}
