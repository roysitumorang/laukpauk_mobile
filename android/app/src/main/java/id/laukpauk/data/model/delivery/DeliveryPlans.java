package id.laukpauk.data.model.delivery;

import androidx.annotation.Nullable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Turns {@code GET buyer/delivery_plans/now} into the schedule tiles ({@link DeliverySchedule}). */
public final class DeliveryPlans {
    private DeliveryPlans() {}

    private static final Locale ID = new Locale("id", "ID");
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm", ID);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, d MMM", ID);
    private static final Pattern GO_DURATION = Pattern.compile("(?:(\\d+)h)?(?:(\\d+)m(?!s))?(?:(\\d+(?:\\.\\d+)?)s)?");

    /**
     * One tile per {@code delivery_times} entry, in order: day ("Hari ini", "Besok", "Lusa" or "Sen, 23 Nov"),
     * session (Pagi / Siang / Sore / Malam), hours ("08:00 - 09:00" with {@code delivery_time_interval}); a slot that
     * has already started is shown closed. {@code code} and {@code raw.delivery_time} keep the backend's text.
     */
    public static List<DeliverySchedule> toSchedules(@Nullable DeliveryPlan plan, ZonedDateTime now) {
        List<DeliverySchedule> list = new ArrayList<>();
        if (plan == null || plan.deliveryTimes == null) {
            return list;
        }
        Duration interval = parseGoDuration(plan.deliveryTimeInterval);
        ZoneId zone = now.getZone();
        for (String text : plan.deliveryTimes) {
            if (text == null) {
                continue;
            }
            ZonedDateTime start;
            try {
                start = OffsetDateTime.parse(text.trim()).atZoneSameInstant(zone);
            } catch (DateTimeParseException e) {
                continue;
            }
            String hours = HOUR.format(start);
            if (!interval.isZero()) {
                hours += " - " + HOUR.format(start.plus(interval));
            }
            Map<String, Object> raw = new LinkedHashMap<>();
            raw.put("kode", text);
            raw.put("day", dayLabel(start.toLocalDate(), now.toLocalDate()));
            raw.put("sessi", session(start.getHour()));
            raw.put("rentangWaktu", hours);
            raw.put("isFull", false);
            raw.put("isOpened", start.isAfter(now));
            raw.put(DeliverySchedule.KEY_DELIVERY_TIME, text);
            raw.put("delivery_plan_id", plan.id);
            raw.put("delivery_plan_name", plan.name);
            list.add(new DeliverySchedule(raw));
        }
        return list;
    }

    static String dayLabel(LocalDate day, LocalDate today) {
        if (day.isEqual(today)) {
            return "Hari ini";
        }
        if (day.isEqual(today.plusDays(1))) {
            return "Besok";
        }
        if (day.isEqual(today.plusDays(2))) {
            return "Lusa";
        }
        return DATE.format(day);
    }

    static String session(int hour) {
        if (hour < 11) {
            return "Pagi";
        }
        if (hour < 15) {
            return "Siang";
        }
        if (hour < 18) {
            return "Sore";
        }
        return "Malam";
    }

    /** Go's {@code time.Duration.String()}: "1h0m0s", "30m0s", "45s", "0s"; anything else is zero. */
    static Duration parseGoDuration(@Nullable String text) {
        if (text == null || text.trim().isEmpty()) {
            return Duration.ZERO;
        }
        Matcher m = GO_DURATION.matcher(text.trim());
        if (!m.matches()) {
            return Duration.ZERO;
        }
        long seconds = 0;
        if (m.group(1) != null) {
            seconds += Long.parseLong(m.group(1)) * 3600;
        }
        if (m.group(2) != null) {
            seconds += Long.parseLong(m.group(2)) * 60;
        }
        if (m.group(3) != null) {
            seconds += (long) Double.parseDouble(m.group(3));
        }
        return Duration.ofSeconds(seconds);
    }

    /** True when a saved delivery plan option has started (its {@code delivery_time} is past). */
    public static boolean isPast(@Nullable Object deliveryTime, ZonedDateTime now) {
        if (deliveryTime == null) {
            return false;
        }
        try {
            return !OffsetDateTime.parse(deliveryTime.toString().trim()).toZonedDateTime().isAfter(now);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
