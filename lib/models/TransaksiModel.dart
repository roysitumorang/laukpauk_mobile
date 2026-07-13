import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/views/pembeli/Keranjang.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/models/ADJDModel.dart';

import 'package:laukpauk/models/CheckoutModel.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/global-components/CustomAlert.dart';

class TransaksiModel {
  static Future<dynamic> getDetailTransaksi(String idTransaksi,
      {String? select}) async {
    String entitasTransaksiQS =
        (select == null) ? '' : 'entitasTransaksi=$select';

    String _params = await HTTPRequest.getQueryString(
        authToken: true, params: entitasTransaksiQS);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Transaksi/getTransaksi/$idTransaksi?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      return dataObject['transaksi'];
    }

    return false;
  }

  static Future<void> removeTransactionSession(BuildContext context) async {
    await LocalStorage.clearCartCode();
    await LocalStorage.clearUsedVoucher();
    await ADJDModel.deleteAlamatDanJadwalDelivery();
    await ADJDModel.deleteCoppiedADJD();

    await CheckoutModel.bersihkanKeranjang();
    Provider.of<HeaderViewModel>(context, listen: false).jumlahCart = 0;
    Provider.of<HeaderViewModel>(context, listen: false).isADJDSet = false;
  }

  static Future<void> melewatiBatasRangeJadwalDeliveryBelumCheckout(
      BuildContext context) async {
    await ADJDModel.deleteAlamatDanJadwalDelivery();

    await CheckoutModel.bersihkanKeranjang();
    Provider.of<HeaderViewModel>(context, listen: false).jumlahCart = 0;
  }

  static Future<int?> jumlahItemDiantar({required String idTransaksi}) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Transaksi/getJumlahItemDiantar/$idTransaksi?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> _dataObject = jsonDecode(httpResponse.body);
      int _jumlahBarangDiantar = _dataObject['jumlahBarangDiantar'];

      return _jumlahBarangDiantar;
    }

    return null;
  }

  static Future<int?> getLastTransactionID() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Transaksi/getLastTransactionID?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> _dataObject = jsonDecode(httpResponse.body);
      int _lastTransactionID = _dataObject['lastTransactionID'];

      return _lastTransactionID;
    }

    return null;
  }

  static Future<Map<String, dynamic>?> belanjaUlang(
      {required String idTransaksi}) async {
    String _cartCode = await LocalStorage.getCartCode();

    Map<String, dynamic>? jadwalDeliveryTerpilih =
        await LocalStorage.savedJadwalDeliveryMap();
    Map<String, dynamic> savedADJD =
        await LocalStorage.getSavedLatitudeAndLongitude();

    String dayDelivery = (jadwalDeliveryTerpilih != null)
        ? jadwalDeliveryTerpilih['day']
        : 'Hari Pengantaran';
    String jadwalDeliveryCode = savedADJD['jadwalDeliveryCode'];

    Map<String, dynamic> _body = {
      'dayDelivery': dayDelivery,
      'jadwalDeliveryCode': jadwalDeliveryCode,
      'cartCode': _cartCode
    };

    Uri _url = Uri.parse(ServerPath.baseURL +
        '/api-v2/' +
        '/Transaksi/belanjaUlang/$idTransaksi');
    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> _dataObject = jsonDecode(httpResponse.body);
      return _dataObject;
    }

    return null;
  }

  static Future<void> responseBelanjaUlang(
      BuildContext context, Map<String, dynamic> response) async {
    String _title = 'Belanja Ulang';
    bool _statusBelanjaUlang = response['statusBelanjaUlang'];
    String? _messageBelanjaUlang = response['message'];

    String _subTitle, _btnText;
    if (_statusBelanjaUlang) {
      _subTitle = 'Berhasil belanja ulang!';
      _btnText = 'OK';
    } else {
      _subTitle = 'Gagal belanja ulang!';
      _btnText = 'Coba Lagi!';
    }

    if (_messageBelanjaUlang != null) {
      _subTitle = _subTitle + ' ' + _messageBelanjaUlang;
    }

    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      CustomButton.button(context, _btnText, () async {
        if (_statusBelanjaUlang) {
          int _jumlahKeranjang = await HeaderModel.getJumlahCart();
          Provider.of<HeaderViewModel>(context, listen: false).jumlahCart =
              _jumlahKeranjang;

          Navigator.pushReplacement(
              context, MaterialPageRoute(builder: (_) => Keranjang()));
        } else {
          Navigator.pop(context);
        }
      }, color: CustomColors.green)
    ]);
    await CustomAlert.textComponent(context,
        title: _title, subTitle: _subTitle, actions: _actions);
  }
}
