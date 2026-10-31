package id.laukpauk.view.pembeli.components.checkout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Port of lib/views/pembeli/components/Checkout/CheckoutGlobalState.dart: a plain holder that the
 * Checkout screen rebuilds on every state change and hands to SubTotal / RingkasanPembayaran.
 * Dart int -> long.
 */
public class CheckoutGlobalState {
    public boolean isCheckoutFormCompleted = false;
    public boolean isWalletOn = false;
    public boolean isPointUsed = false;
    public long totalKeseluruhan = 0;
    public long point = 0;
    public long subTotal = 0;
    public long potonganDompet = 0;
    public long potonganVoucher = 0;
    public long ongkir = 0;
    public long minimalPointTukarPoint = 0;
    public double totalDapatPoint = 0;
    public String catatanTambahan;
    public String cartCode;
    public String kodePromo;
    /** bool? : null = no promo code answer yet. */
    public Boolean kodePromoStatus;
    public String kodePromoMessage;
    public long kodePromoDiskon = 0;
    public JsonObject detailUser;
    /** Map? : null when no voucher is used. */
    public JsonObject usedVoucher;
    /** Map? : null when no delivery schedule is saved. */
    public JsonObject jadwalDelivery;
    /** dynamic: the literal false (or null) when there is no category discount, otherwise a map with 'diskon'. */
    public JsonElement diskonKategori;
    public long nilaiPointOut = 0;

    public CheckoutGlobalState(String cartCode, boolean isPointUsed, boolean isWalletOn,
                               boolean isCheckoutFormCompleted, long totalKeseluruhan, long point,
                               long subTotal, long potonganDompet, long potonganVoucher, long ongkir,
                               JsonObject detailUser, String catatanTambahan, JsonObject usedVoucher,
                               JsonObject jadwalDelivery, double totalDapatPoint,
                               long minimalPointTukarPoint, JsonElement diskonKategori,
                               long nilaiPointOut, String kodePromo, Boolean kodePromoStatus,
                               long kodePromoDiskon, String kodePromoMessage) {
        this.cartCode = cartCode;
        this.isPointUsed = isPointUsed;
        this.isWalletOn = isWalletOn;
        this.isCheckoutFormCompleted = isCheckoutFormCompleted;
        this.totalKeseluruhan = totalKeseluruhan;
        this.point = point;
        this.subTotal = subTotal;
        this.potonganDompet = potonganDompet;
        this.potonganVoucher = potonganVoucher;
        this.ongkir = ongkir;
        this.detailUser = detailUser;
        this.catatanTambahan = catatanTambahan;
        this.usedVoucher = usedVoucher;
        this.jadwalDelivery = jadwalDelivery;
        this.totalDapatPoint = totalDapatPoint;
        this.minimalPointTukarPoint = minimalPointTukarPoint;
        this.diskonKategori = diskonKategori;
        this.nilaiPointOut = nilaiPointOut;
        this.kodePromo = kodePromo;
        this.kodePromoStatus = kodePromoStatus;
        this.kodePromoDiskon = kodePromoDiskon;
        this.kodePromoMessage = kodePromoMessage;
    }

    /** Dart: `_diskonKategori != false` (a null answer is treated as "no discount" instead of crashing). */
    public boolean hasDiskonKategori() {
        return diskonKategori != null && diskonKategori.isJsonObject();
    }
}
