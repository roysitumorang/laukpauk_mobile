import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:image_picker/image_picker.dart' show PickedFile;
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class TambahProdukModel {
  static Future<bool> saveProduk(BuildContext context,
      {String idProduk = '',
      required Map<String, dynamic> dataProdukMap}) async {
    if (idProduk.isNotEmpty) {
      idProduk = '/$idProduk';
    }

    String nama = dataProdukMap['nama'];
    String jenis = dataProdukMap['jenis'];
    String jenisLC = jenis.toLowerCase();
    String keterangan = dataProdukMap['keterangan'];
    PickedFile? imageProdukData = dataProdukMap['imageProdukData'];

    Map<String, dynamic> dataProduk = {
      'nama': nama,
      'jenisProduk': jenis,
      'keterangan': keterangan
    };
    if (imageProdukData != null) {
      List<int> imageProdukDataListInt = await imageProdukData.readAsBytes();
      dataProduk[(jenisLC == 'satuan') ? 'produkImg' : 'paketImg'] =
          base64Encode(imageProdukDataListInt);
    }

    if (jenis == 'satuan') {
      String kategori = dataProdukMap['kategori'];
      dataProduk['kategori'] = kategori;
    } else {
      String harga = dataProdukMap['harga'];
      String diskon = dataProdukMap['diskon'];

      dataProduk['harga'] = harga;
      dataProduk['diskon'] = diskon;
    }

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: dataProduk);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Produk/saveProduk$idProduk?' + _params);
    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool saveProduk = dataObject['saveProduk'];

      return saveProduk;
    }

    return false;
  }
}
