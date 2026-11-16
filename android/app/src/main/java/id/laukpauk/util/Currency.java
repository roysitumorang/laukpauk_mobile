package id.laukpauk.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Port of lib/libraries/Currency.dart (intl NumberFormat.currency with locale 'id':
 * '.' as thousands separator, ',' as decimal separator).
 */
public final class Currency {
    private Currency() {}

    private static DecimalFormat format(int decimalDigits) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimalDigits > 0) {
            pattern.append('.');
            for (int i = 0; i < decimalDigits; i++) {
                pattern.append('0');
            }
        }
        return new DecimalFormat(pattern.toString(), symbols);
    }

    /** Dart: Currency.formatRupiah(number, decimalPlaces) -> "Rp. 15.000". */
    public static String formatRupiah(long number, int decimalPlaces) {
        String sign = number < 0 ? "-" : "";
        return sign + "Rp. " + format(decimalPlaces).format(Math.abs(number));
    }

    /** Dart: Currency.formatUnit(number) -> "15.000". */
    public static String formatUnit(long number) {
        return formatUnit(number, null);
    }

    /** Dart: Currency.formatUnit(number, satuan: 'kg') -> "15.000 kg". */
    public static String formatUnit(long number, String unit) {
        String sign = number < 0 ? "-" : "";
        String text = sign + format(0).format(Math.abs(number));
        return unit != null ? text + " " + unit : text;
    }
}
