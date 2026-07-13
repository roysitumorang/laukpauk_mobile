import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

class PasarModel {
  static Future<List> getProdukInThisPasar(String idPasar,
      {required String useLimit, String? startFrom, String? cariProduk}) async {
    //auth token dipakai untuk mencegah mendapatkan produk user yang sedang login jika dia seorang penjual
    //hal ini bertujuan supaya user tsb tidak bisa order produknya sendiri

    bool isADJDSet = await LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
    Map<String, dynamic> adjdSet;
    String jadwalDeliveryCodeQS = '';
    if (isADJDSet) {
      adjdSet = await LocalStorage.getSavedLatitudeAndLongitude();
      jadwalDeliveryCodeQS =
          'jadwalDeliveryCode=' + adjdSet['jadwalDeliveryCode'];
    }

    String _startFromQS = '';
    if (startFrom != null) {
      _startFromQS = '&startFrom=$startFrom';
    }

    String _useLimitQS = '&useLimit=' + useLimit;
    String _cariProdukQS = '';
    if (cariProduk != null) {
      _cariProdukQS = '&cariProduk=' + cariProduk;
    }

    String _params = await HTTPRequest.getQueryString(
        authToken: true,
        params: '$jadwalDeliveryCodeQS$_startFromQS$_useLimitQS$_cariProdukQS');
    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Pasar/getProdukInThisPasar/$idPasar?' +
        _params);
    Response httpResponse = await get(_url);

    print(_params);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List listProduk = dataObject['listProduk'];

      return listProduk;
    }

    return [];
  }
}
