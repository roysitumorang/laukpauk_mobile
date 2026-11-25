package id.laukpauk.data.model.voucher;

import androidx.annotation.Nullable;

import com.squareup.moshi.Json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** A buyer voucher ({@code GET buyer/vouchers} -> {@code data[]}). */
public class Voucher {
    /** {@code voucher_type}: reduces the delivery cost. */
    public static final int TYPE_DELIVERY_COST = 0;
    /** {@code voucher_type}: reduces the shopping cost. */
    public static final int TYPE_SHOPPING_COST = 1;

    public String id;
    @Nullable public String name;
    /** Full URL of the picture (or a legacy file name). */
    @Nullable public String photo;
    @Json(name = "minimum_purchase")
    public long minimumShopping;
    /** The deduction in rupiah. */
    @Json(name = "discount")
    public long value;
    /** How many times a buyer may use it. */
    @Json(name = "quota_per_buyer")
    public long quotaPerBuyer;
    @Json(name = "voucher_type")
    public int voucherType;
    @Json(name = "voucher_type_name")
    @Nullable public String voucherTypeName;
    @Nullable public String code;
    @Nullable public String description;
    /** e.g. "2026-11-01T00:00:00+07:00". */
    @Json(name = "effective_start_date")
    @Nullable public String effectiveStartDate;
    @Json(name = "effective_end_date")
    @Nullable public String effectiveEndDate;
    /** Time of day with its UTC offset, e.g. "0001-01-01T08:00:00+07:00" (the date part is not used). */
    @Json(name = "effective_start_time")
    @Nullable public String effectiveStartTime;
    @Json(name = "effective_end_time")
    @Nullable public String effectiveEndTime;

    /** What the voucher reduces, as the checkout shows it: "ongkir" or "belanja". */
    public String type() {
        return voucherType == TYPE_DELIVERY_COST ? "ongkir" : "belanja";
    }

    /**
     * Whether the buyer can use it at {@code nowMillis}, as the backend's CreateTransaction checks it:
     * {@code effective_start_date <= now <= effective_end_date} (instants), the time of day of now within
     * {@code effective_start_time}..{@code effective_end_time} (each compared in its own UTC offset), and, as the
     * voucher list filters it, {@code shoppingCost >= minimum_purchase}. A missing bound does not limit.
     */
    public boolean canUse(long shoppingCost, long nowMillis) {
        if (shoppingCost < minimumShopping) {
            return false;
        }
        Long start = instant(effectiveStartDate);
        Long end = instant(effectiveEndDate);
        if ((start != null && nowMillis < start) || (end != null && nowMillis > end)) {
            return false;
        }
        Integer from = secondOfDay(effectiveStartTime);
        Integer until = secondOfDay(effectiveEndTime);
        return (from == null || secondOfDay(nowMillis, offsetMillis(effectiveStartTime)) >= from)
                && (until == null || secondOfDay(nowMillis, offsetMillis(effectiveEndTime)) <= until);
    }

    /** The zone of the effective times: "WIB" (+07:00), "WITA" (+08:00), "WIT" (+09:00), otherwise "UTC±hh:mm". */
    public String zone() {
        Integer offset = offsetMillis(effectiveStartTime);
        if (offset == null) {
            return "";
        }
        int minutes = offset / 60000;
        if (minutes == 7 * 60) {
            return "WIB";
        }
        if (minutes == 8 * 60) {
            return "WITA";
        }
        if (minutes == 9 * 60) {
            return "WIT";
        }
        if (minutes == 0) {
            return "UTC";
        }
        int abs = Math.abs(minutes);
        return String.format(Locale.US, "UTC%s%02d:%02d", minutes < 0 ? "-" : "+", abs / 60, abs % 60);
    }

    /** Milliseconds since the epoch of an RFC 3339 time ("2026-11-30T00:00:00+07:00", fractions allowed), or null. */
    @Nullable
    static Long instant(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim().replaceFirst("\\.\\d+", "");
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            f.setLenient(false);
            return f.parse(v).getTime();
        } catch (ParseException | IllegalArgumentException e) {
            return null;
        }
    }

    /** The UTC offset of an RFC 3339 time in milliseconds ("Z" is 0), or null. */
    @Nullable
    static Integer offsetMillis(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if (v.endsWith("Z") || v.endsWith("z")) {
            return 0;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("([+-])(\\d{2}):?(\\d{2})$").matcher(v);
        if (!m.find()) {
            return null;
        }
        int minutes = Integer.parseInt(m.group(2)) * 60 + Integer.parseInt(m.group(3));
        return ("-".equals(m.group(1)) ? -minutes : minutes) * 60000;
    }

    /** The time of day written in an RFC 3339 time, in seconds, or null. */
    @Nullable
    static Integer secondOfDay(@Nullable String value) {
        String t = time(value);
        if (t == null) {
            return null;
        }
        return Integer.parseInt(t.substring(0, 2)) * 3600 + Integer.parseInt(t.substring(3, 5)) * 60
                + Integer.parseInt(t.substring(6, 8));
    }

    static int secondOfDay(long millis, @Nullable Integer offsetMillis) {
        long local = millis + (offsetMillis == null ? 0 : offsetMillis);
        return (int) (Math.floorMod(local, 86_400_000L) / 1000);
    }

    /**
     * The deduction: {@code discount}, up to the delivery cost when {@code voucher_type} is 0, otherwise up to the
     * shopping cost.
     */
    public long deduction(long shoppingCost, long deliveryCost) {
        long cost = voucherType == TYPE_DELIVERY_COST ? deliveryCost : shoppingCost;
        return Math.max(0, Math.min(value, cost));
    }

    /** "08:00" .. "17:00" of the effective times (empty when missing). */
    public String timeFrom() {
        String t = time(effectiveStartTime);
        return t == null ? "" : t.substring(0, 5);
    }

    public String timeUntil() {
        String t = time(effectiveEndTime);
        return t == null ? "" : t.substring(0, 5);
    }


    /** "HH:mm:ss" of a Go time ("0000-01-01T08:00:00Z"), or null. */
    @Nullable
    static String time(@Nullable String value) {
        if (value == null) {
            return null;
        }
        int t = value.indexOf('T');
        String rest = t >= 0 ? value.substring(t + 1) : value;
        return rest.length() >= 8 && rest.charAt(2) == ':' && rest.charAt(5) == ':' ? rest.substring(0, 8) : null;
    }

    /** {@code effective_start_date} as "1 November 2026" (empty when missing). */
    public String validFrom() {
        return longDate(effectiveStartDate);
    }

    /** {@code effective_end_date} as "30 November 2026" (empty when missing). */
    public String validUntil() {
        return longDate(effectiveEndDate);
    }

    static String longDate(@Nullable String date) {
        String d = day(date);
        if (d == null) {
            return "";
        }
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            in.setLenient(false);
            return new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID")).format(in.parse(d));
        } catch (ParseException e) {
            return d;
        }
    }

    @Nullable
    private static String day(@Nullable String date) {
        return date == null || date.length() < 10 ? null : date.substring(0, 10);
    }
}
