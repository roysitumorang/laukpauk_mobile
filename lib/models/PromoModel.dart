import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

class PromoModel {
  static Future<List> getProdukPromo() async {
    bool isADJDSet = await LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
    if (isADJDSet) {
      Map<String, dynamic> adjdSet =
          await LocalStorage.getSavedLatitudeAndLongitude();
      String latitude = adjdSet['latitude'];
      String longitude = adjdSet['longitude'];
      String jadwalDeliveryCode = adjdSet['jadwalDeliveryCode'];

      String _params = await HTTPRequest.getQueryString(
          params:
              'latitude=$latitude&longitude=$longitude&jadwalDeliveryCode=$jadwalDeliveryCode',
          authToken: true);

      Uri _url = Uri.parse(ServerPath.apiBaseURL +
          '/Promo/getPromoAroundOfThisLatLong?' +
          _params);
      Response httpResponse = await get(_url);

      if (httpResponse.statusCode == 200) {
        Map dataObject = jsonDecode(httpResponse.body);
        List produkPromo = dataObject['produkPromo'];

        return produkPromo;
      }
    }
    return [];
  }

  static Future<dynamic> getDetailPromoItem(String idPromoItem) async {
    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Promo/getPromoItem/$idPromoItem');
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> serverResponse = jsonDecode(httpResponse.body);
      dynamic promoItem = serverResponse['promoItem'];

      return promoItem;
    }
    return false;
  }
}
