import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/views/pembeli/Keranjang.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

class PaketModel {
  static SnackBar addToCartSnackBar(
      {required BuildContext context, required String text}) {
    SizeConfig().initSize(context);

    SnackBar _snackBar = SnackBar(
        duration: Duration(seconds: 3),
        action: SnackBarAction(
            label: 'Lihat Keranjang',
            textColor: CustomColors.green,
            onPressed: () => Navigator.push(
                context, MaterialPageRoute(builder: (_) => Keranjang()))),
        content: Text(text,
            style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)));

    return _snackBar;
  }

  static Future<dynamic> detailPaket(String? idPaket) async {
    idPaket = idPaket ?? '';

    String _params =
        await HTTPRequest.getQueryString(params: 'idPaket=$idPaket');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Produk/detailPaket?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      dynamic detailPaket = dataObject['detailPaket'];

      return detailPaket;
    }

    return false;
  }

  static Future<bool> addToCart(String idPaket) async {
    String cartCode = await LocalStorage.getCartCode();

    Map<String, dynamic>? jadwalDeliveryTerpilih =
        await LocalStorage.savedJadwalDeliveryMap();
    Map<String, dynamic> savedADJD =
        await LocalStorage.getSavedLatitudeAndLongitude();

    String dayDelivery = (jadwalDeliveryTerpilih != null)
        ? jadwalDeliveryTerpilih['day']
        : 'Hari Pengantaran';
    String jadwalDeliveryCode = savedADJD['jadwalDeliveryCode'];

    Map<String, String> _body = {
      'cartCode': cartCode,
      'idProduk': idPaket,
      'jenisProduk': 'paket',
      'quantity': '1',
      'jadwalDeliveryCode': jadwalDeliveryCode,
      'dayDelivery': dayDelivery
    };
    Map<String, dynamic> _httpBody = await HTTPRequest.postBody(body: _body);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Keranjang/saveCart');
    Response httpResponse = await post(_url, body: _httpBody);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusAddCart = dataObject['statusAddCart'];

      return statusAddCart;
    }
    return false;
  }

  static Future<bool?> adjdNotSetAlert(BuildContext context,
      {String namaProduk = ''}) async {
    SizeConfig().initSize(context);

    return showDialog<bool?>(
        context: context,
        builder: (context) => AlertDialog(
                title: Text('Lauk Pauk',
                    style: TextStyle(fontSize: SizeConfig.horizontalBlock * 4)),
                content: Text(
                    'Mohon maaf, untuk dapat mengorder produk ${namaProduk.toUpperCase()} anda harus menentukan alamat dan jadwal delivery terlebih dahulu !',
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5)),
                actions: [
                  TextButton(
                      onPressed: () {
                        Navigator.pop(context, false);
                      },
                      child: Text('Tutup')),
                  TextButton(
                      onPressed: () {
                        Navigator.pop(context, true);
                      },
                      child: Text('Tentukan Alamat & Jadwal'))
                ]));
  }
}
