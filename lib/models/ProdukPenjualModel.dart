import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class ProdukPenjualModel {
  static Future<bool> toggleProdukPalingLaris(
      String idProduk, String jenisProduk) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    String endPoint = (jenisProduk.toLowerCase() == 'satuan')
        ? 'toggleProdukPalingLaris'
        : 'togglePaketPalingLaris';
    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Produk/$endPoint/$idProduk?' + _params);

    Response httpResponse = await get(_url);
    print(httpResponse.body);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);

      if (jenisProduk.toLowerCase() == 'satuan') {
        bool statusToggleProdukPalingLaris =
            dataObject['statusToggleProdukPalingLaris'];

        return statusToggleProdukPalingLaris;
      } else {
        bool statusTogglePaketPalingLaris =
            dataObject['statusTogglePaketPalingLaris'];

        return statusTogglePaketPalingLaris;
      }
    }

    return false;
  }

  static Future<bool> ubahKategoriProduk(
      String idProduk, String idKategoriProdukBaru) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(
        body: {'kategoriProduk': idKategoriProdukBaru});

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Produk/ubahKategoriProduk/$idProduk?' +
        _params);
    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool statusUbahKategoriProduk = dataObject['statusUbahKategoriProduk'];

      return statusUbahKategoriProduk;
    }

    return false;
  }

  static Future<bool> ubahDeskripsiProduk(
      String idProduk, String jenisProduk, String deskripsiProduk) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);
    String endPoint = (jenisProduk.toLowerCase() == 'satuan')
        ? 'ubahDeskripsiProduk'
        : 'ubahDeskripsiPaket';

    Map<String, dynamic> _data = {
      (jenisProduk.toLowerCase() == 'satuan')
          ? 'deskripsiProduk'
          : 'deskripsiPaket': deskripsiProduk
    };

    Map<String, dynamic> _body = await HTTPRequest.postBody(body: _data);

    Uri url = Uri.parse(
        ServerPath.apiBaseURL + '/Produk/$endPoint/$idProduk?' + _params);
    Response httpResponse = await post(url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      if (jenisProduk.toLowerCase() == 'satuan') {
        bool statusUbahDeskripsiProduk =
            dataObject['statusUbahDeskripsiProduk'];

        return statusUbahDeskripsiProduk;
      } else {
        bool statusUbahDeskripsiPaket = dataObject['statusUbahDeskripsiPaket'];

        return statusUbahDeskripsiPaket;
      }
    }

    return false;
  }

  static Future<bool?> showKonfirmasi(BuildContext context,
      {String? title, String? content, List<TextButton>? actions}) async {
    SizeConfig().initSize(context);
    return showDialog<bool?>(
        context: context,
        builder: (context) => AlertDialog(
            title: Text(title ?? '',
                style: TextStyle(fontSize: SizeConfig.horizontalBlock * 4)),
            content: Text(content ?? '',
                style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5)),
            actions: actions));
  }

  static Future<bool> hapusProduk(String idProduk) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Produk/hapusProduk/$idProduk?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool statusHapusProduk = dataObject['statusHapusProduk'];

      return statusHapusProduk;
    }

    return false;
  }

  static Future<bool> hapusPaket(String idProduk) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Produk/hapusPaket/$idProduk?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool statusHapusPaket = dataObject['statusHapusPaket'];

      return statusHapusPaket;
    }

    return false;
  }
}
