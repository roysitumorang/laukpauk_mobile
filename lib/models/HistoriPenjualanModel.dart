import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class HistoriPenjualanModel {
  static Future<List> getHistoriPenjualan(
      {String? idPenjual, String? select, String? statusTransaksi}) async {
    idPenjual = (idPenjual == null) ? '' : '/$idPenjual';

    String selectTransaksi =
        (select == null) ? '' : 'selectTransaksi=' + select;
    statusTransaksi =
        (statusTransaksi == null) ? '' : '&statusTransaksi=$statusTransaksi';

    String _params = await HTTPRequest.getQueryString(
        params: '$selectTransaksi$statusTransaksi', authToken: true);
    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Penjual/getHistoriPenjualan$idPenjual?' +
        _params);

    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      List historiPenjualan = dataObject['historiPenjualan'];

      return historiPenjualan;
    }

    return [];
  }

  static Future<int> getTotalTransaksiForThisPenjual(String idTransaksi,
      {String? idPenjual}) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);
    idPenjual = (idPenjual == null) ? '' : '/$idPenjual';

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Transaksi/getTotalTransaksiForThisPenjual/$idTransaksi$idPenjual?' +
        _params);

    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      int totalTransaksi = dataObject['totalTransaksi'];

      return totalTransaksi;
    }

    return 0;
  }
}
