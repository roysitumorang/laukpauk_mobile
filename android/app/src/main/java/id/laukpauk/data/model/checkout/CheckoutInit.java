package id.laukpauk.data.model.checkout;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

/**
 * Everything the Checkout screen needs once the cart total is known (legacy
 * CheckoutActivity._dataInitialization). Assembled by CheckoutRepository from several requests.
 */
public class CheckoutInit {
    /** totalBelanja >= 1; false = the cart is below the minimum purchase. */
    public boolean enough;
    /** The shipping type answer arrived (legacy only shows the page then). */
    public boolean complete;
    public long totalBelanja;
    public long totalOngkir;
    public long jumlahBarang;
    public long totalDompetSaya;
    public long point;
    public long minimalPointTukarPoint;
    public String cartCode = "";
    public String penjelasanOngkir = "";
    @Nullable public WalletConfiguration walletConfiguration;
    @Nullable public CheckoutResponses.JenisOngkir jenisOngkir;
    /** Dynamic server object ({diskon, ...}) forwarded verbatim to the checkout request; null = no discount. */
    @Nullable public JsonObject diskonKategori;
    public String nama = "";
    public String nomorHP = "";
    public String alamat = "";
}
