package id.laukpauk;

import java.text.NumberFormat;
import java.util.Locale;

/** Indonesian Rupiah/quantity formatting equivalent to the old Currency helper. */
public final class RupiahFormatter {
    private static final Locale INDONESIAN = new Locale("id", "ID");

    private RupiahFormatter() { }

    public static String rupiah(long amount, int fractionDigits) {
        NumberFormat format = NumberFormat.getNumberInstance(INDONESIAN);
        format.setMinimumFractionDigits(Math.max(0, fractionDigits));
        format.setMaximumFractionDigits(Math.max(0, fractionDigits));
        return "Rp. " + format.format(amount);
    }

    public static String quantity(long amount, String unit) {
        NumberFormat format = NumberFormat.getNumberInstance(INDONESIAN);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        String number = format.format(amount);
        return unit == null || unit.trim().isEmpty() ? number : number + " " + unit;
    }
}
