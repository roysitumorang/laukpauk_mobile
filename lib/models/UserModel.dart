import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class UserModel {
  static Future<dynamic> getDetailActiveUser() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getDetailUser?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      return dataObject['detailUser'];
    }

    return false;
  }

  static Future<List> getHistoriPoint() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getRiwayatPoint?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      List _historiPoint = dataObject['riwayatPoint'];

      return _historiPoint;
    }

    return [];
  }
}
