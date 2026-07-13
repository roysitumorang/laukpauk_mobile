import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

class ROModel {
  static Future<Map<String, dynamic>> addToCartThesePenjual(
      String idTransaksi, List<String> listPenjual) async {
    String cartCode = await LocalStorage.getCartCode();
    String selectedSellersString = listPenjual.join(', ');

    Map<String, dynamic> httpBody = {
      'idTransaksi': idTransaksi,
      'cartCode': cartCode,
      'selectedSellers': selectedSellersString
    };

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: httpBody);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/RO/addToCartThesePenjual?' + _params);
    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> _serverResponse = jsonDecode(httpResponse.body);

      return _serverResponse;
    }

    return {'statusAddToCart': false};
  }

  static Future<List> getItemTransaksiPerPenjual(String idTransaksi) async {
    Map<String, dynamic> savedADJD =
        await LocalStorage.getSavedLatitudeAndLongitude();

    String latDelivery = savedADJD['latitude'] ?? '';
    String longDelivery = savedADJD['longitude'] ?? '';
    String jadwalDeliveryCode = savedADJD['jadwalDeliveryCode'] ?? '';

    String _params = await HTTPRequest.getQueryString(
        authToken: true,
        params:
            'idTransaksi=$idTransaksi&latDelivery=$latDelivery&longDelivery=$longDelivery&jadwalDeliveryCode=$jadwalDeliveryCode');

            String _stringURL = ServerPath.apiBaseURL + '/RO/getItemTransaksi?' + _params;

    Uri _url =
        Uri.parse(_stringURL);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> serverResponse = jsonDecode(httpResponse.body);
      List itemTransaksi = serverResponse['itemTransaksi'];

      return itemTransaksi;
    }

    return [];
  }
}
