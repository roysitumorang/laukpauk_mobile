import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class ResepModel {
  static dynamic _returnResponse(Response response) {
    int _statusCode = response.statusCode;
    if (_statusCode == 200) {
      dynamic _responseJSON = jsonDecode(response.body);
      List _listMenu = _responseJSON['dataResep'];
      return _listMenu;
    }
  }

  static Future<List> getResep(BuildContext context) async {
    List _responseJSON = [];

    try {
      Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Resep/getResep');
      Response _httpResponse = await get(_url);

      _responseJSON = _returnResponse(_httpResponse);
    } catch (exception) {
      String _stringException = exception.toString();

      showDialog(
          context: context,
          builder: (showDialogContext) => AlertDialog(
              title: Text('Exception Occured'),
              content: Text(_stringException)));
    }

    return _responseJSON;
  }

  static Future<List> getResepItem(String idResep) async {
    String entitasResepItem = 'id, namaProduk, takaran, idProduk';
    String _params = await HTTPRequest.getQueryString(
        params: 'entitasResepItem=$entitasResepItem');

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Resep/getResepItemFromThisResep/$idResep?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      return dataObject['dataResepItem'];
    }

    return [];
  }

  static Future<bool> belanjaResep(
      BuildContext context, Map<String, dynamic> httpBody) async {
    Map<String, dynamic> _httpBody = await HTTPRequest.postBody(body: httpBody);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Keranjang/belanjaResep');
    Response httpResponse = await post(_url, body: _httpBody);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool statusBelanjaResep = dataObject['statusBelanjaResep'];

      return statusBelanjaResep;
    }

    return false;
  }
}
