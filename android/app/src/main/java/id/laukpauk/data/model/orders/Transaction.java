package id.laukpauk.data.model.orders;

import com.squareup.moshi.Json;

/**
 * A buyer transaction, both as a row of the history list (User/getRiwayatTransaksi) and as the detail
 * (Transaksi/getTransaksi). Numbers arrive as numeric strings; the lenient Moshi adapters turn them into ints.
 */
public class Transaction {
    public String id;
    public String cartCode;
    @Json(name = "nomorTransaksi")
    public String transactionNumber;
    public String status;
    public String statusReason;
    @Json(name = "catatan")
    public String note;
    public String dayDelivery;
    @Json(name = "jadwalDelivery")
    public String deliverySchedule;
    @Json(name = "formattedTanggalDelivery")
    public String formattedDeliveryDate;
    @Json(name = "waktuCheckout")
    public String checkoutTime;
    @Json(name = "alamatDelivery")
    public String deliveryAddress;
    @Json(name = "alamatDetailLengkap")
    public String fullAddressDetail;
    @Json(name = "metodePembayaran")
    public String paymentMethod;
    @Json(name = "namaMetodePembayaran")
    public String paymentMethodName;
    @Json(name = "jenisVoucher")
    public String voucherType;

    @Json(name = "totalBelanja")
    public int shoppingTotal;
    @Json(name = "ongkir")
    public int shippingFee;
    @Json(name = "potonganWallet")
    public int walletDiscount;
    @Json(name = "nilaiVoucher")
    public int voucherValue;
    public int pointOut;
    @Json(name = "diskonPromo")
    public int promoDiscount;
    /** The unique transfer code for "tf_bca". */
    @Json(name = "moreDataMetodePembayaran")
    public int paymentMethodMoreData;
    @Json(name = "refundedOngkir")
    public int refundedShippingFee;
    @Json(name = "refundedUangBelanja")
    public int refundedShoppingMoney;

    /** (shoppingTotal + ongkir) - all deductions; the unique code is not included. */
    public int overallTotal() {
        return (shoppingTotal + shippingFee)
                - (voucherValue + walletDiscount + pointOut + promoDiscount);
    }

    public boolean isTfBca() {
        return paymentMethod != null && paymentMethod.toLowerCase().equals("tf_bca");
    }

    /** Total shown in the list / to be paid: {@link #overallTotal()} plus the unique code for "tf_bca". */
    public int totalPayment() {
        return overallTotal() + (isTfBca() ? paymentMethodMoreData : 0);
    }

    public String statusLower() {
        return status == null ? "" : status.toLowerCase();
    }
}
