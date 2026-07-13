import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class PointModel {
  static const String createdFrom_admin = 'admin';
  static const String createdFrom_transaksi = 'transaksi';

  static Future<dynamic> getPoint({String? queryString}) async {
    String _queryString = 'historiPoint=true&point=true';
    if (queryString != null) {
      _queryString = '&' + queryString;
    }

    String _params =
        await HTTPRequest.getQueryString(authToken: true, params: _queryString);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Point/getPoint?' + _params);
    Response _httpResponse = await get(_url);

    if (_httpResponse.statusCode == 200) {
      Map<String, dynamic> _body = jsonDecode(_httpResponse.body);
      dynamic _response = _body['response'];

      return _response;
    }

    return false;
  }
}
