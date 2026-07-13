import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/models/AuthModel.dart';

class NotifikasiModel {
  static Future<List> getUpdateNotifikasi() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getNotifikasi?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      List notifikasi = dataObject['notifikasi'];

      return notifikasi;
    }

    return [];
  }

  static Future<List> getNotifikasiKhusus({String? idUser}) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);
    String userID = (idUser == null) ? '' : '/' + idUser;
    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/User/getNotifikasiKhusus$userID?' + _params);

    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      List notifikasi = dataObject['notifikasi'];

      return notifikasi;
    }

    return [];
  }

  static Future<int> getJumlahNotifikasiKhusus(String statusNotifikasi) async {
    bool isLogin = await AuthModel.isLogin();
    if (isLogin) {
      String _params = await HTTPRequest.getQueryString(
          params: 'status=$statusNotifikasi', authToken: true);

      Uri _url = Uri.parse(
          ServerPath.apiBaseURL + '/User/getJumlahNotifikasiKhusus?' + _params);
      Response httpResponse = await get(_url);

      if (httpResponse.statusCode == 200) {
        dynamic dataObject = jsonDecode(httpResponse.body);
        int jumlahNotifikasi = dataObject['jumlahNotifikasi'];

        return jumlahNotifikasi;
      }
    }
    return 0;
  }

  static Future<int> getJumlahNotifikasi(String statusNotifikasi) async {
    bool isLogin = await AuthModel.isLogin();
    if (isLogin) {
      String _params = await HTTPRequest.getQueryString(
          params: 'status=$statusNotifikasi', authToken: true);

      Uri _url = Uri.parse(
          ServerPath.apiBaseURL + '/User/getJumlahNotifikasi?' + _params);
      Response httpResponse = await get(_url);

      if (httpResponse.statusCode == 200) {
        dynamic dataObject = jsonDecode(httpResponse.body);
        int jumlahNotifikasi = dataObject['jumlahNotifikasi'];

        return jumlahNotifikasi;
      }
    }
    return 0;
  }

  static Future<dynamic> getDetailNotifikasiGlobal(String idNotifikasi) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Notifikasi/getNotifikasi/$idNotifikasi?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> serverResponse = jsonDecode(httpResponse.body);
      dynamic detailNotifikasiGlobal = serverResponse['notifikasi'];

      return detailNotifikasiGlobal;
    }
    return false;
  }

  static Future<bool> setAs(String idNotifikasiItem, String setAsParam,
      String jenisNotifikasi) async {
    Map<String, dynamic> _body = {
      'idNotifikasiItem': idNotifikasiItem,
      'setAs': setAsParam,
      'jenisNotifikasi': jenisNotifikasi
    };

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _httpBody = await HTTPRequest.postBody(body: _body);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Notifikasi/setAs?' + _params);
    Response httpResponse = await post(_url, body: _httpBody);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> serverResponse = jsonDecode(httpResponse.body);
      bool statusSetAs = serverResponse['statusSetAs'];

      return statusSetAs;
    }
    return false;
  }
}
