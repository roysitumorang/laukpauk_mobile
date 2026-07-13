import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/views/penjual/components/TokoSaya/UpdateLokasiBerjualan.dart';

class TokoSayaModel {
  static Future<bool?> showModalUbahLokasiBerjualan(
      BuildContext context, Map<String, dynamic> dataToko) async {
    return await showModalBottomSheet(
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.only(
                topLeft: Radius.circular(15.0),
                topRight: Radius.circular(15.0))),
        context: context,
        builder: (context) {
          return UpdateLokasiBerjualan(dataToko, (statusUbahLokasiBerjualan) {
            return Navigator.pop(context, statusUbahLokasiBerjualan);
          });
        });
  }

  static Future<bool> ubahLokasiBerjualan(
      String idPenjual, Map<String, dynamic> lokasiBaruBerjualan) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Penjual/ubahLokasiBerjualan/$idPenjual?' +
        _params);
    Map<String, dynamic> _body =
        await HTTPRequest.postBody(body: lokasiBaruBerjualan);

    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool statusUbahLokasiBerjualan = dataObject['statusUbahLokasiBerjualan'];

      return statusUbahLokasiBerjualan;
    }
    return false;
  }
}
