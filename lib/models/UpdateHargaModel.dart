import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class UpdateHargaModel {
  static Future<bool> hapusSatuan(String idSatuanProduk) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/HargaProduk/hapusHargaProduk/$idSatuanProduk?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusHapusHargaProduk = dataObject['statusHapusHargaProduk'];

      return statusHapusHargaProduk;
    }

    return false;
  }

  static Future<bool> konfirmasiHapusSatuan(
      BuildContext context, Map<String, dynamic> dataSatuan) async {
    String _title = 'Satuan Produk';
    String _subTitle =
        'Apakah anda yakin akan menghapus satuan ' + dataSatuan['nama'] + ' ?';
    bool _konfirmasi = await CustomAlert.konfirmasi(context,
            title: _title, subTitle: _subTitle) ??
        false;

    return _konfirmasi;
  }

  static Future<bool> editSatuanProduk(
      String? idSatuanProduk, Map<String, dynamic> dataSatuanProduk) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body =
        await HTTPRequest.postBody(body: dataSatuanProduk);

    String urlOptions = (idSatuanProduk != null) ? '/$idSatuanProduk' : '';

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/HargaProduk/saveHargaProduk$urlOptions?' +
        _params);
    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusSaveHargaProduk = dataObject['statusSaveHargaProduk'];

      return statusSaveHargaProduk;
    }

    return false;
  }
}
