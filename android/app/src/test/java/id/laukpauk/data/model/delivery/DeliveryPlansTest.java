package id.laukpauk.data.model.delivery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import id.laukpauk.data.remote.ApiClient;

/** {@code GET buyer/delivery_plans/now} → schedule tiles. */
public class DeliveryPlansTest {

    private static final ZoneId JAKARTA = ZoneId.of("Asia/Jakarta");

    private static DeliveryPlan parse(String json) throws Exception {
        return ApiClient.createMoshi().adapter(DeliveryResponses.PlanNow.class).fromJson(json).data;
    }

    @Test
    public void deliveryTimesBecomeTiles() throws Exception {
        DeliveryPlan plan = parse("{\"status_code\":200,\"data\":{\"id\":\"p1\",\"record_status\":0,"
                + "\"name\":\"Weekday pagi\",\"weekday\":1,"
                + "\"transaction_window_min\":\"2026-11-20T00:00:00+07:00\","
                + "\"transaction_window_max\":\"2026-11-20T08:00:00+07:00\","
                + "\"delivery_time_interval\":\"1h0m0s\",\"same_day\":true,\"next_day\":true,"
                + "\"delivery_times\":[\"2026-11-20T07:00:00+07:00\",\"2026-11-20T10:00:00+07:00\","
                + "\"2026-11-21T16:00:00+07:00\",\"2026-11-22T19:00:00+07:00\",\"2026-11-24T08:00:00+07:00\"]}}");
        ZonedDateTime now = ZonedDateTime.of(2026, 11, 20, 7, 30, 0, 0, JAKARTA);
        List<DeliverySchedule> tiles = DeliveryPlans.toSchedules(plan, now);
        assertEquals(5, tiles.size());

        DeliverySchedule past = tiles.get(0);
        assertEquals("Hari ini", past.day);
        assertEquals("Pagi", past.session);
        assertEquals("07:00 - 08:00", past.timeRange);
        assertFalse(past.isSelectable());

        DeliverySchedule today = tiles.get(1);
        assertTrue(today.isSelectable());
        assertEquals("2026-11-20T10:00:00+07:00", today.code);
        assertEquals("2026-11-20T10:00:00+07:00", today.raw.get(DeliverySchedule.KEY_DELIVERY_TIME));
        assertEquals("p1", today.raw.get("delivery_plan_id"));
        assertEquals("2026-11-20T10:00:00+07:00", today.readable());

        assertEquals("Besok", tiles.get(2).day);
        assertEquals("Sore", tiles.get(2).session);
        assertEquals("Lusa", tiles.get(3).day);
        assertEquals("Malam", tiles.get(3).session);
        assertEquals("Sel, 24 Nov", tiles.get(4).day);
    }

    @Test
    public void goDurations() {
        assertEquals(Duration.ofHours(1), DeliveryPlans.parseGoDuration("1h0m0s"));
        assertEquals(Duration.ofMinutes(90), DeliveryPlans.parseGoDuration("1h30m0s"));
        assertEquals(Duration.ofMinutes(30), DeliveryPlans.parseGoDuration("30m0s"));
        assertEquals(Duration.ZERO, DeliveryPlans.parseGoDuration("0s"));
        assertEquals(Duration.ZERO, DeliveryPlans.parseGoDuration(null));
        assertEquals(Duration.ZERO, DeliveryPlans.parseGoDuration("soon"));
    }

    @Test
    public void noPlanNoTiles() {
        assertTrue(DeliveryPlans.toSchedules(null, ZonedDateTime.now(JAKARTA)).isEmpty());
    }

    @Test
    public void savedOptionExpiresWhenItStarts() {
        ZonedDateTime now = ZonedDateTime.of(2026, 11, 20, 10, 0, 1, 0, JAKARTA);
        assertTrue(DeliveryPlans.isPast("2026-11-20T10:00:00+07:00", now));
        assertFalse(DeliveryPlans.isPast("2026-11-20T11:00:00+07:00", now));
        assertFalse(DeliveryPlans.isPast(null, now));
    }
}
