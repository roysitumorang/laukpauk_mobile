import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class KodePromoModel {
  static Future<Map<String, dynamic>?> cekKodePromo({required String kodePromo}) async {

      String _params = await HTTPRequest.getQueryString(
          authToken: true);

      Uri _url = Uri.parse(ServerPath.apiBaseURL +
          '/Promo/cekKodePromo/$kodePromo?' +
          _params);
      Response httpResponse = await get(_url);

      if (httpResponse.statusCode == 200) {
        Map<String, dynamic> _respond = jsonDecode(httpResponse.body);

        return _respond;
      }

    return null;
  }

}
