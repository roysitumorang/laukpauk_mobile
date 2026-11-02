package id.laukpauk.ui.checkout;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import id.laukpauk.data.model.checkout.CheckoutResponses;
import id.laukpauk.data.model.checkout.Voucher;
import id.laukpauk.data.model.checkout.WalletConfiguration;

/** The computed totals of the Checkout screen (legacy CheckoutGlobalState + dataRingkasanPembayaran). Immutable. */
public final class CheckoutSummary {
    public boolean isFormCompleted;
    public boolean isPointUsed;
    public boolean isWalletOn;
    public boolean walletDibatalkan;
    public long jumlahBarang;
    /** Basket total before wallet / voucher / shipping. */
    public long subTotal;
    public long totalOngkir;
    public long potonganWallet;
    public long potonganVoucher;
    public long potonganDiskon;
    public long nilaiPointOut;
    public long point;
    public long minimalPointTukarPoint;
    public long totalKeseluruhan;
    public String penjelasanOngkir = "";
    @Nullable public Voucher usedVoucher;
    @Nullable public String statusVoucher;
    @Nullable public WalletConfiguration walletConfiguration;
    @Nullable public CheckoutResponses.JenisOngkir jenisOngkir;
    @Nullable public JsonObject diskonKategori;
    @Nullable public Boolean kodePromoStatus;
    @Nullable public String kodePromoMessage;
}
