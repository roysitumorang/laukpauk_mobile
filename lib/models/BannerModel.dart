import 'dart:convert';

import 'package:http/http.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class BannerModel {
  static Future<List> getBanner() async {
    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Banner/getBanner');
    Response _httpResponse = await get(_url);

    if (_httpResponse.statusCode == 200) {
      Map<String, dynamic> _dataBannerObject = json.decode(_httpResponse.body);
      List _dataBanner = _dataBannerObject['listBanner'];

      return _dataBanner;
    }
    return [];
  }
}
