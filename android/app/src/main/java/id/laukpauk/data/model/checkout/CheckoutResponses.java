package id.laukpauk.data.model.checkout;

import com.squareup.moshi.Json;


import java.util.List;

/** Response envelopes of the checkout endpoints. */
public final class CheckoutResponses {
    private CheckoutResponses() {}

    public static class TotalShippingFee {
        @Json(name = "totalOngkir")
        public long totalShippingFee;
    }

    public static class CartQuantity {
        @Json(name = "jumlahKeranjang")
        public long cartCount;
    }

    /** GET Transaksi/getJenisOngkir: the fields are at the top level of the answer. */
    public static class ShippingFeeType {
        /** "perkm" when the address is outside the coverage area, otherwise the flat delivery cost applies. */
        @Json(name = "jenisOngkir")
        public String shippingFeeType;
        /** Distance the courier travels, in km (only meaningful for "perkm"). */
        @Json(name = "jarak")
        public double distance;
    }

    public static class WalletConfig {
        /** null when the server answers false. */
        public WalletConfiguration walletConfiguration;
    }

    public static class CategoryDiscount {
        /** Dynamic: an object, or false when there is no category discount. */
        @Json(name = "diskonKategori")
        public Object categoryDiscount;
    }

    public static class Point {
        public long point;
    }

    public static class PointResponse {
        /** null when the server answers false. */
        public Point response;
    }

    public static class ScheduleOpened {
        @Json(name = "isJadwalDeliveryOpened")
        public boolean isDeliveryScheduleOpened;
    }

    public static class VoucherList {
        public List<Voucher> listVoucher;
    }

    public static class StatusVoucher {
        public String statusVoucher;
    }

    public static class Promo {
        @Json(name = "diskon")
        public long discount;
    }

    public static class PromoData {
        public Promo promo;
    }

    /** GET Promo/cekKodePromo/{kode}. */
    public static class PromoCode {
        public boolean status;
        public String message;
        public PromoData data;
    }
}
