package id.laukpauk.data.model.checkout;

import com.squareup.moshi.Json;

/** Response envelopes of the checkout endpoints. */
public final class CheckoutResponses {
    private CheckoutResponses() {}

    public static class ScheduleOpened {
        @Json(name = "isJadwalDeliveryOpened")
        public boolean isDeliveryScheduleOpened;
    }
}
