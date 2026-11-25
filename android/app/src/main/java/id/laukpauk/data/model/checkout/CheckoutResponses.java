package id.laukpauk.data.model.checkout;

import com.squareup.moshi.Json;


import java.util.List;

/** Response envelopes of the checkout endpoints. */
public final class CheckoutResponses {
    private CheckoutResponses() {}

    public static class CartQuantity {
        @Json(name = "jumlahKeranjang")
        public long cartCount;
    }

    public static class CategoryDiscount {
        /** Dynamic: an object, or false when there is no category discount. */
        @Json(name = "diskonKategori")
        public Object categoryDiscount;
    }

    public static class ScheduleOpened {
        @Json(name = "isJadwalDeliveryOpened")
        public boolean isDeliveryScheduleOpened;
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
