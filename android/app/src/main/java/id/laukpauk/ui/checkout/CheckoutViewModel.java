package id.laukpauk.ui.checkout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import id.laukpauk.data.Resource;
import id.laukpauk.data.model.checkout.CheckoutInit;
import id.laukpauk.data.model.checkout.CheckoutResponses;
import id.laukpauk.data.model.checkout.JadwalPengiriman;
import id.laukpauk.data.model.checkout.Voucher;
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

    /** Redirect code (besides CheckoutRepository.STEP_*): the cart is below the minimum purchase. */
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
    private final MutableLiveData<Event<Boolean>> voucherRejected = new MutableLiveData<>();
    private final MutableLiveData<CheckoutSummary> summary = new MutableLiveData<>();
    private final MutableLiveData<JadwalPengiriman> jadwal = new MutableLiveData<>();
    private final MutableLiveData<Boolean> orderBusy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<OrderOutcome>> orderResult = new MutableLiveData<>();
    private LiveData<Resource<List<PaymentChannel>>> channels;

    private boolean started;
    private CheckoutInit init;
    private String nama = "", hp = "", alamat = "", catatan = "";
    private Voucher usedVoucher;
    private String statusVoucher;
    private boolean pointUsed;
    private String kodePromo = "";
    private Boolean kodePromoStatus;
    private String kodePromoMessage;
    private long kodePromoDiskon;
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

    public LiveData<Event<Boolean>> voucherRejected() {
        return voucherRejected;
    }

    public LiveData<CheckoutSummary> summary() {
        return summary;
    }

    public LiveData<JadwalPengiriman> jadwal() {
        return jadwal;
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
                nama = d.nama;
                hp = d.nomorHP;
                alamat = d.alamat;
                reloadJadwal();
                formLoaded.setValue(new Event<>(true));
                recompute();
                phase.setValue(PHASE_READY);
            });
        });
    }

    /** Re-reads the saved delivery schedule (after the delivery selection changed). */
    public void reloadJadwal() {
        jadwal.setValue(checkout.savedJadwal());
    }

    // ------------------------------------------------------------------ form / options

    public String nama() {
        return nama;
    }

    public String hp() {
        return hp;
    }

    public String alamat() {
        return alamat;
    }

    public String catatan() {
        return catatan;
    }

    public void setNama(String v) {
        nama = v;
        recompute();
    }

    public void setHp(String v) {
        hp = v;
        recompute();
    }

    public void setAlamat(String v) {
        alamat = v;
        recompute();
    }

    public void setCatatan(String v) {
        catatan = v;
        recompute();
    }

    public void setPointUsed(boolean used) {
        pointUsed = used;
        recompute();
    }

    public void removeVoucher() {
        checkout.clearUsedVoucher();
        usedVoucher = null;
        statusVoucher = null;
        recompute();
    }

    public LiveData<Resource<List<Voucher>>> vouchers() {
        return checkout.getVouchers(init != null ? init.totalBelanja : 0);
    }

    /** A voucher was picked in the sheet: remember it, unless its value exceeds the total to pay. */
    public void chooseVoucher(final Voucher voucher) {
        checkout.selectVoucher(voucher.id, res -> {
            String status = res.data != null ? res.data : "kadaluarsa";
            CheckoutSummary s = summary.getValue();
            if (s == null || voucher.nilai <= s.totalKeseluruhan) {
                usedVoucher = voucher;
                statusVoucher = status;
                recompute();
            } else {
                voucherRejected.setValue(new Event<>(true));
            }
        });
    }

    public Voucher usedVoucher() {
        return usedVoucher;
    }

    /** Called on every change of the promo code field. */
    public void onKodePromo(final String kode) {
        final int seq = ++promoSeq;
        kodePromo = kode;
        if (kode.isEmpty()) {
            kodePromoStatus = null;
            kodePromoMessage = null;
            kodePromoDiskon = 0;
            recompute();
            return;
        }
        checkout.cekKodePromo(kode, res -> {
            if (seq != promoSeq) {
                return; // a newer code was typed meanwhile
            }
            CheckoutResponses.KodePromo p = res.isSuccess() ? res.data : null;
            if (p != null) {
                kodePromoStatus = p.status;
                kodePromoMessage = p.message;
                kodePromoDiskon = p.data != null && p.data.promo != null ? p.data.promo.diskon : 0;
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
        s.isFormCompleted = nama.trim().length() >= 1 && hp.trim().length() >= 1 && alamat.trim().length() >= 1;
        long totalBelanja = init.totalBelanja;
        long totalOngkir = init.totalOngkir;

        long nilaiVoucher = 0;
        if (usedVoucher != null && statusVoucher != null && statusVoucher.toLowerCase().equals("aktif")) {
            nilaiVoucher = usedVoucher.nilai;
        }
        long nilaiDiskonKategori = init.diskonKategori != null ? Json.longVal(init.diskonKategori, "diskon") : 0;

        long potonganWallet = 0;
        boolean isWalletOn = false;
        boolean walletDibatalkan = false;
        if (init.walletConfiguration != null) {
            isWalletOn = init.walletConfiguration.isWalletOn;
            if (isWalletOn && totalBelanja >= init.walletConfiguration.minGrandTotalTransactionForWallet) {
                long potonganDompetConfig = init.walletConfiguration.walletDiscount;
                if (init.totalDompetSaya >= potonganDompetConfig) {
                    potonganWallet = potonganDompetConfig;
                } else {
                    walletDibatalkan = true;
                }
            }
        }

        long subTotalBersih = totalBelanja - potonganWallet - nilaiVoucher - nilaiDiskonKategori;
        long nilaiPointOut = pointUsed ? init.point : 0;
        if (nilaiPointOut >= subTotalBersih) {
            nilaiPointOut = subTotalBersih;
        }

        s.isPointUsed = pointUsed;
        s.isWalletOn = isWalletOn;
        s.walletDibatalkan = walletDibatalkan;
        s.jumlahBarang = init.jumlahBarang;
        s.subTotal = totalBelanja;
        s.totalOngkir = totalOngkir;
        s.potonganWallet = potonganWallet;
        s.potonganVoucher = nilaiVoucher;
        s.potonganDiskon = nilaiDiskonKategori;
        s.nilaiPointOut = nilaiPointOut;
        s.point = init.point;
        s.minimalPointTukarPoint = init.minimalPointTukarPoint;
        s.totalKeseluruhan = (totalBelanja + totalOngkir)
                - (nilaiVoucher + potonganWallet + nilaiPointOut + nilaiDiskonKategori + kodePromoDiskon);
        s.penjelasanOngkir = init.penjelasanOngkir;
        s.usedVoucher = usedVoucher;
        s.statusVoucher = statusVoucher;
        s.walletConfiguration = init.walletConfiguration;
        s.jenisOngkir = init.jenisOngkir;
        s.diskonKategori = init.diskonKategori;
        s.kodePromoStatus = kodePromoStatus;
        s.kodePromoMessage = kodePromoMessage;
        summary.setValue(s);
    }

    // ------------------------------------------------------------------ order

    /** The body of the checkout request (legacy SubTotal.onBayar), or null when no schedule is saved. */
    public Map<String, String> buildCheckoutData() {
        JadwalPengiriman j = jadwal.getValue();
        CheckoutSummary s = summary.getValue();
        if (j == null || s == null || init == null) {
            return null;
        }
        Map<String, String> m = new LinkedHashMap<>();
        m.put("namaPenerima", nama.trim());
        m.put("nomorHPPenerima", hp.trim());
        m.put("alamatDetailLengkap", alamat.trim());
        m.put("catatanTambahan", catatan.trim());
        m.put("jadwalPengiriman", nz(j.day) + " | " + nz(j.rentangWaktu));
        m.put("cartCode", init.cartCode);
        m.put("voucher", usedVoucher == null ? "0" : nz(usedVoucher.id));
        m.put("latDelivery", checkout.latDelivery());
        m.put("longDelivery", checkout.longDelivery());
        m.put("jadwalDeliveryCode", nz(j.kode));
        m.put("isPointUsed", pointUsed ? "true" : "false");
        JsonObject dk = init.diskonKategori;
        m.put("diskonKategori", dk != null ? Json.toJson(dk) : "");
        m.put("kodePromo", kodePromo);
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
    public void buatOrder(final PaymentChannel channel, final String moreData) {
        final Map<String, String> data = buildCheckoutData();
        if (data == null || Boolean.TRUE.equals(orderBusy.getValue())) {
            return;
        }
        orderBusy.setValue(true);
        payment.buatOrder(channel, data, moreData, res -> {
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
}
