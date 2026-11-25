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

    /** What the voucher reduces, as the checkout shows it: "ongkir" or "belanja". */
    public String type() {
        return voucherType == TYPE_DELIVERY_COST ? "ongkir" : "belanja";
    }

    /**
     * "aktif" when {@code today} (yyyy-MM-dd) is within the effective dates, otherwise "kadaluarsa"; a missing date
     * does not limit.
     */
    public String status(String today) {
        String start = day(effectiveStartDate);
        String end = day(effectiveEndDate);
        boolean started = start == null || start.compareTo(today) <= 0;
        boolean notEnded = end == null || end.compareTo(today) >= 0;
        return started && notEnded ? "aktif" : "kadaluarsa";
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
