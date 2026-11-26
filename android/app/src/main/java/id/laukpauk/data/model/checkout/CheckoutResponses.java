package id.laukpauk.data.model.checkout;

import com.squareup.moshi.Json;


import java.util.List;

/** Response envelopes of the checkout endpoints. */
public final class CheckoutResponses {
    private CheckoutResponses() {}

    public static class CategoryDiscount {
        /** Dynamic: an object, or false when there is no category discount. */
        @Json(name = "diskonKategori")
        public Object categoryDiscount;
    }

    public static class ScheduleOpened {
        @Json(name = "isJadwalDeliveryOpened")
        public boolean isDeliveryScheduleOpened;
    }
}
