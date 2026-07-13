import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class KontenModel {
  static Future<List> getProdukKonten(String idKonten, {int? limit}) async {
    String limitQS = (limit == null) ? '' : 'limit=${limit.toString()}';

    String _params = await HTTPRequest.getQueryString(params: limitQS);
    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Konten/getKontenItemFromThisKonten/$idKonten?' +
        _params);

    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      List kontenItem = dataObject['kontenItem'];

      return kontenItem;
    }

    return [];
  }

  static Future<List> getKontenAplikasi({String page = 'homepage'}) async {
    String _params = await HTTPRequest.getQueryString(params: 'page=$page');
    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Konten/getKonten?' + _params);

    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List kontenAplikasi = dataObject['dataKonten'];

      return kontenAplikasi;
    }

    return [];
  }
}
