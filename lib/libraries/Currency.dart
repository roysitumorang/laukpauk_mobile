import 'package:intl/intl.dart';

class Currency {
  static String formatRupiah(int number, int angkaDibelakangKoma) {
    return NumberFormat.currency(
            locale: 'id', decimalDigits: angkaDibelakangKoma, symbol: 'Rp. ')
        .format(number);
  }

  static String formatSatuan(int number, {String? satuan}) {
    String space = (satuan != null) ? ' ' : '';
    satuan = (satuan != null) ? satuan : '';

    return NumberFormat.currency(locale: 'id', decimalDigits: 0, symbol: '')
            .format(number) +
        space +
        satuan;
  }
}
