package id.laukpauk.data.model.checkout;

import com.squareup.moshi.Json;

import java.util.Map;

import androidx.annotation.Nullable;


/**
 * Everything the Checkout screen needs once the cart total is known (legacy
 * CheckoutActivity._dataInitialization). Assembled by CheckoutRepository from several requests.
 */
public class CheckoutInit {
    /** shoppingTotal >= 1; false = the cart is below the minimum purchase. */
    public boolean enough;
    /** The shipping type answer arrived (legacy only shows the page then). */
    public boolean complete;
    @Json(name = "totalBelanja")
    public long shoppingTotal;
    @Json(name = "totalOngkir")
    public long totalShippingFee;
    @Json(name = "jumlahBarang")
    public long goodsCount;
    @Json(name = "totalDompetSaya")
    public long myWalletTotal;
    public long point;
    @Json(name = "minimalPointTukarPoint")
    public long minimumPointRedeem;
    public String cartCode = "";
    @Json(name = "penjelasanOngkir")
    public String shippingFeeExplanation = "";
    @Nullable public WalletConfiguration walletConfiguration;
    @Json(name = "jenisOngkir")
    @Nullable public CheckoutResponses.ShippingFeeType shippingFeeType;
    /** Dynamic server object ({diskon, ...}) forwarded verbatim to the checkout request; null = no discount. */
    @Json(name = "diskonKategori")
    @Nullable public Map<String, Object> categoryDiscount;
    @Json(name = "nama")
    public String name = "";
    @Json(name = "nomorHP")
    public String phoneNumber = "";
    @Json(name = "alamat")
    public String address = "";
}
