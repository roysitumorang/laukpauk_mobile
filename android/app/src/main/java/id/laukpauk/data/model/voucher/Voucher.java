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
    /** e.g. "2026-11-01T00:00:00Z". */
    @Json(name = "effective_start_date")
    @Nullable public String effectiveStartDate;
    @Json(name = "effective_end_date")
    @Nullable public String effectiveEndDate;
    /** Time of day only, e.g. "0000-01-01T08:00:00Z" (the date part is meaningless). */
    @Json(name = "effective_start_time")
    @Nullable public String effectiveStartTime;
    @Json(name = "effective_end_time")
    @Nullable public String effectiveEndTime;

    /** What the voucher reduces, as the checkout shows it: "ongkir" or "belanja". */
    public String type() {
        return voucherType == TYPE_DELIVERY_COST ? "ongkir" : "belanja";
    }

    /**
     * Whether the buyer can use it now: {@code shoppingCost} is at least {@code minimum_purchase}, {@code today}
     * (yyyy-MM-dd) is within {@code effective_start_date}..{@code effective_end_date} and {@code timeOfDay}
     * (HH:mm:ss) within {@code effective_start_time}..{@code effective_end_time}, all inclusive. A missing bound does
     * not limit.
     */
    public boolean canUse(long shoppingCost, String today, String timeOfDay) {
        return shoppingCost >= minimumShopping
                && within(today, day(effectiveStartDate), day(effectiveEndDate))
                && within(timeOfDay, time(effectiveStartTime), time(effectiveEndTime));
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

    private static boolean within(String value, @Nullable String from, @Nullable String until) {
        return (from == null || from.compareTo(value) <= 0) && (until == null || until.compareTo(value) >= 0);
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
