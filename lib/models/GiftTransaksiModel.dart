import 'dart:convert';

import 'package:http/http.dart';

import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/element-class/GiftTransaksi.dart';

class GiftTransaksiModel {
  static Future<List<GiftTransaksi>> getGiftTransaksi({String? cartCode}) async {
    List<GiftTransaksi> _giftTransaksi  = [];

    String _cartCode  = await LocalStorage.getCartCode();
    if(cartCode != null){
      _cartCode   = cartCode;
    }

    String _params = await HTTPRequest.getQueryString(params: 'cartCode=$_cartCode');

    String _urlString = ServerPath.apiBaseURL +
        '/gift/gift-transaksi?' +
        _params;
    Uri _url = Uri.parse(_urlString);
      print(_urlString);

    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);

      print(dataObject);

      List _rawGift = dataObject['giftTransaksi'];
      List<GiftTransaksi> _gift = _rawGift.map((giftItem) => GiftTransaksi.fromJson(giftItem)).toList();

      return _gift;
    }

    return _giftTransaksi;
  }
}
