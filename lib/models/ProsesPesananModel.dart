import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class ProsesPesananModel {
  static Future<bool> prosesPesanan(String idTransaksi) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Transaksi/prosesPesanan/$idTransaksi?' +
        _params);
    Response httpResponse = await post(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      return dataObject['statusProsesPesanan'];
    }
    return false;
  }

  static Future<List> getPesananMasukItem(String idTransaksi) async {
    String pesananEntity =
        'id, quantity, namaProduk, namaSatuan, harga, diskon, fotoProduk, catatan, jenisProduk, status';
    String _params = await HTTPRequest.getQueryString(
        authToken: true, params: 'pesananEntity=$pesananEntity');

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Penjual/getPesananMasukItem/$idTransaksi?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      return dataObject['pesananMasukItem'];
    }

    return [];
  }
}
