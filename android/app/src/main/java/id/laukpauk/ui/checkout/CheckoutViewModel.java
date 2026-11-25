package id.laukpauk.ui.checkout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.checkout.CheckoutInit;
import id.laukpauk.data.model.checkout.CheckoutResponses;
import id.laukpauk.data.model.checkout.ShippingSchedule;
import id.laukpauk.data.model.voucher.Voucher;
import id.laukpauk.data.model.payment.CheckoutResponse;
import id.laukpauk.data.model.payment.PaymentChannel;
import id.laukpauk.data.repository.CheckoutRepository;
import id.laukpauk.data.repository.PaymentRepository;
import id.laukpauk.util.Json;

/**
 * State of the whole Checkout screen and of the payment sheets (replaces the legacy CheckoutGlobalState and
 * the State fields of CheckoutActivity): loaded data, form, voucher, points, promo code, totals and the order.
 */
public class CheckoutViewModel extends ViewModel {

    public static final int PHASE_LOADING = 0;
    public static final int PHASE_READY = 1;
    public static final int PHASE_INCOMPLETE = 2;

    /** Redirect code (besides CheckoutRepository.STEP_*): the cart's total is 0 (nothing to check out). */
    public static final int REDIRECT_NOT_ENOUGH = 100;

    /** Result of placing an order. */
    public static final class OrderOutcome {
        public final String channelCodeLC;
        public final CheckoutResponse response;
        public final Map<String, String> dataCheckout;

        OrderOutcome(String channelCodeLC, CheckoutResponse response, Map<String, String> dataCheckout) {
            this.channelCodeLC = channelCodeLC;
            this.response = response;
            this.dataCheckout = dataCheckout;
        }
    }

    private final CheckoutRepository checkout;
    private final PaymentRepository payment;

    private final MutableLiveData<Integer> phase = new MutableLiveData<>(PHASE_LOADING);
    private final MutableLiveData<Event<Integer>> redirect = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> formLoaded = new MutableLiveData<>();
    private final MutableLiveData<CheckoutSummary> summary = new MutableLiveData<>();
    private final MutableLiveData<ShippingSchedule> schedule = new MutableLiveData<>();
    private final MutableLiveData<Boolean> orderBusy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<OrderOutcome>> orderResult = new MutableLiveData<>();
    private LiveData<Resource<List<PaymentChannel>>> channels;

    private boolean started;
    private CheckoutInit init;
    private String name = "", phone = "", address = "", note = "";
    private Voucher usedVoucher;
    private final MutableLiveData<Event<Voucher>> voucherRejected = new MutableLiveData<>();
    private String statusVoucher;
    private boolean pointUsed;
    private String promoCode = "";
    private Boolean promoCodeStatus;
    private String promoCodeMessage;
    private long promoCodeDiscount;
    private int promoSeq;

    public CheckoutViewModel(CheckoutRepository checkout, PaymentRepository payment) {
        this.checkout = checkout;
        this.payment = payment;
    }

    public LiveData<Integer> phase() {
        return phase;
    }

    public LiveData<Event<Integer>> redirect() {
        return redirect;
    }

    /** The form fields were filled from the server: copy them into the inputs. */
    public LiveData<Event<Boolean>> formLoaded() {
        return formLoaded;
    }


    public LiveData<CheckoutSummary> summary() {
        return summary;
    }

    public LiveData<ShippingSchedule> schedule() {
        return schedule;
    }

    public LiveData<Boolean> orderBusy() {
        return orderBusy;
    }

    public LiveData<Event<OrderOutcome>> orderResult() {
        return orderResult;
    }

    // ------------------------------------------------------------------ loading

    /** Loads once (survives rotation); a redirect resets it so coming back loads again. */
    public void start() {
        if (!started) {
            started = true;
            load();
        }
    }

    public void refresh() {
        load();
    }

    private void load() {
        phase.setValue(PHASE_LOADING);
        checkout.checkPrerequisites(res -> {
            int step = res.data != null ? res.data : CheckoutRepository.STEP_OK;
            if (step != CheckoutRepository.STEP_OK) {
                started = false;
                redirect.setValue(new Event<>(step));
                return;
            }
            checkout.loadInit(r -> {
                CheckoutInit d = r.data;
                if (d == null) {
                    return;
                }
                if (!d.enough) {
                    started = false;
                    redirect.setValue(new Event<>(REDIRECT_NOT_ENOUGH));
                    return;
                }
                if (!d.complete) {
                    phase.setValue(PHASE_INCOMPLETE); // legacy: the loading view stays
                    return;
                }
                init = d;
                name = d.name;
                phone = d.phoneNumber;
                address = d.address;
                reloadSchedule();
                formLoaded.setValue(new Event<>(true));
                recompute();
                phase.setValue(PHASE_READY);
            });
        });
    }

    /** Re-reads the saved delivery schedule (after the delivery selection changed). */
    public void reloadSchedule() {
        schedule.setValue(checkout.savedSchedule());
    }

    // ------------------------------------------------------------------ form / options

    public String name() {
        return name;
    }

    public String phone() {
        return phone;
    }

    public String address() {
        return address;
    }

    public String note() {
        return note;
    }

    public void setName(String v) {
        name = v;
        recompute();
    }

    public void setPhone(String v) {
        phone = v;
        recompute();
    }

    public void setNote(String v) {
        note = v;
        recompute();
    }

    /** Points and a voucher exclude each other (the backend applies the points first and then ignores the voucher). */
    public void setPointUsed(boolean used) {
        pointUsed = used;
        if (used && usedVoucher != null) {
            checkout.clearUsedVoucher();
            usedVoucher = null;
        }
        recompute();
    }

    /** A voucher whose minimum_purchase the shopping cost does not reach (shown as a dialog). */
    public LiveData<Event<Voucher>> voucherRejected() {
        return voucherRejected;
    }

    public void removeVoucher() {
        checkout.clearUsedVoucher();
        usedVoucher = null;
        statusVoucher = null;
        recompute();
    }

    /** The vouchers usable for the cart grand total (the shopping total). */
    public LiveData<Resource<List<Voucher>>> vouchers() {
        return checkout.getVouchers(init != null ? init.shoppingTotal : 0);
    }

    /** A voucher was picked in the sheet: used when the shopping cost reaches its minimum_purchase. */
    public void chooseVoucher(final Voucher voucher) {
        if (!voucher.canUse(init != null ? init.shoppingTotal : 0)) {
            voucherRejected.setValue(new Event<>(voucher));
            return;
        }
        checkout.selectVoucher(voucher);
        usedVoucher = voucher;
        pointUsed = false; // the backend applies either the points or a voucher
        recompute();
    }

    public Voucher usedVoucher() {
        return usedVoucher;
    }

    /** Called on every change of the promo code field. */
    public void onPromoCode(final String code) {
        final int seq = ++promoSeq;
        promoCode = code;
        if (code.isEmpty()) {
            promoCodeStatus = null;
            promoCodeMessage = null;
            promoCodeDiscount = 0;
            recompute();
            return;
        }
        checkout.checkPromoCode(code, res -> {
            if (seq != promoSeq) {
                return; // a newer code was typed meanwhile
            }
            CheckoutResponses.PromoCode p = res.isSuccess() ? res.data : null;
            if (p != null) {
                promoCodeStatus = p.status;
                promoCodeMessage = p.message;
                promoCodeDiscount = p.data != null && p.data.promo != null ? p.data.promo.discount : 0;
            }
            recompute();
        });
    }

    // ------------------------------------------------------------------ totals

    private void recompute() {
        if (init == null) {
            return;
        }
        CheckoutSummary s = new CheckoutSummary();
        s.isFormCompleted = name.trim().length() >= 1 && phone.trim().length() >= 1 && address.trim().length() >= 1;
        long shoppingTotal = init.shoppingTotal;
        long totalShippingFee = init.totalShippingFee;

        // the voucher: discount on the delivery cost (voucher_type 0) or the shopping cost
        long voucherValue = 0;
        statusVoucher = null;
        if (usedVoucher != null && !init.voucherEnabled) {
            checkout.clearUsedVoucher();
            usedVoucher = null;
        }
        if (usedVoucher != null) {
            boolean usable = usedVoucher.canUse(shoppingTotal);
            statusVoucher = usable ? "aktif" : "tidak berlaku";
            voucherValue = usable ? usedVoucher.deduction(shoppingTotal, totalShippingFee) : 0;
        }
        long shoppingVoucher = usedVoucher != null && usedVoucher.voucherType != Voucher.TYPE_DELIVERY_COST
                ? voucherValue : 0;
        long categoryDiscountValue = init.categoryDiscount != null ? Json.longVal(init.categoryDiscount, "diskon") : 0;

        long netSubTotal = shoppingTotal - shoppingVoucher - categoryDiscountValue;
        long pointOutValue = pointUsed ? init.point : 0;
        if (pointOutValue >= netSubTotal) {
            pointOutValue = netSubTotal;
        }
        // the wallet is always on: it pays what is left, up to the balance (no minimum total)
        long walletDiscount = walletDeduction(init.myWalletTotal, (shoppingTotal + totalShippingFee)
                - (voucherValue + pointOutValue + categoryDiscountValue + promoCodeDiscount));

        s.isPointUsed = pointUsed;
        s.voucherEnabled = init.voucherEnabled;
        s.goodsCount = init.goodsCount;
        s.subTotal = shoppingTotal;
        s.totalShippingFee = totalShippingFee;
        s.walletDiscount = walletDiscount;
        s.voucherDiscount = voucherValue;
        s.discountAmount = categoryDiscountValue;
        s.pointOutValue = pointOutValue;
        s.point = init.point;
        s.minimumPointRedeem = init.minimumPointRedeem;
        s.overallTotal = (shoppingTotal + totalShippingFee)
                - (voucherValue + walletDiscount + pointOutValue + categoryDiscountValue + promoCodeDiscount);
        s.shippingFeeExplanation = init.shippingFeeExplanation;
        s.usedVoucher = usedVoucher;
        s.statusVoucher = statusVoucher;
        s.categoryDiscount = init.categoryDiscount;
        s.promoCodeStatus = promoCodeStatus;
        s.promoCodeMessage = promoCodeMessage;
        summary.setValue(s);
    }

    // ------------------------------------------------------------------ order

    /** The body of the checkout request (legacy SubTotal.onPay), or null when no schedule is saved. */
    public Map<String, String> buildCheckoutData() {
        ShippingSchedule j = schedule.getValue();
        CheckoutSummary s = summary.getValue();
        if (j == null || s == null || init == null) {
            return null;
        }
        Map<String, String> m = new LinkedHashMap<>();
        m.put("namaPenerima", name.trim());
        m.put("nomorHPPenerima", phone.trim());
        m.put("alamatDetailLengkap", address.trim());
        m.put("catatanTambahan", note.trim());
        m.put("jadwalPengiriman", nz(j.day) + " | " + nz(j.timeRange));
        m.put("cartCode", init.cartCode);
        m.put("voucher", usedVoucher == null ? "0" : nz(usedVoucher.id));
        m.put("latDelivery", checkout.latDelivery());
        m.put("longDelivery", checkout.longDelivery());
        m.put("jadwalDeliveryCode", nz(j.code));
        m.put("isPointUsed", pointUsed ? "true" : "false");
        Map<String, Object> dk = init.categoryDiscount;
        m.put("diskonKategori", dk != null ? Json.toJson(dk) : "");
        m.put("kodePromo", promoCode);
        return m;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /** The payment channels, loaded once. */
    public LiveData<Resource<List<PaymentChannel>>> channels() {
        if (channels == null) {
            channels = payment.getPaymentChannels();
        }
        return channels;
    }

    /** Places the order with the chosen channel; the answer arrives in {@link #orderResult()}. */
    public void placeOrder(final PaymentChannel channel, final String moreData) {
        final Map<String, String> data = buildCheckoutData();
        if (data == null || Boolean.TRUE.equals(orderBusy.getValue())) {
            return;
        }
        orderBusy.setValue(true);
        payment.placeOrder(channel, data, moreData, res -> {
            orderBusy.setValue(false);
            Map<String, String> dataCheckout = new LinkedHashMap<>(data);
            dataCheckout.put("metodePembayaran", nz(channel.channel_code));
            dataCheckout.put("namaMetodePembayaran", nz(channel.name));
            // never keep the card token (it holds the CVN) in the data handed to the next screen
            dataCheckout.put("moreDataMetodePembayaran",
                    "kartu_kredit".equalsIgnoreCase(channel.channel_code) ? "" : nz(moreData));
            orderResult.setValue(new Event<>(new OrderOutcome(nz(channel.channel_code).toLowerCase(),
                    res.data != null ? res.data : CheckoutResponse.failed(), dataCheckout)));
        });
    }

    /** The wallet deduction: what is left to pay, up to the wallet balance (never negative). */
    static long walletDeduction(long walletBalance, long due) {
        return Math.max(0, Math.min(walletBalance, due));
    }
}
