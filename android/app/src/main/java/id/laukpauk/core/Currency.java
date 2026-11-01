package id.laukpauk.core;

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

    /** Dart: Currency.formatRupiah(number, angkaDibelakangKoma) -> "Rp. 15.000". */
    public static String formatRupiah(long number, int angkaDibelakangKoma) {
        String sign = number < 0 ? "-" : "";
        return sign + "Rp. " + format(angkaDibelakangKoma).format(Math.abs(number));
    }

    /** Dart: Currency.formatSatuan(number) -> "15.000". */
    public static String formatSatuan(long number) {
        return formatSatuan(number, null);
    }

    /** Dart: Currency.formatSatuan(number, satuan: 'kg') -> "15.000 kg". */
    public static String formatSatuan(long number, String satuan) {
        String sign = number < 0 ? "-" : "";
        String text = sign + format(0).format(Math.abs(number));
        return satuan != null ? text + " " + satuan : text;
    }
}
