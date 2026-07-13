import 'dart:convert';

import 'package:http/http.dart';

import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/element-class/Gift.dart';

class GiftModel {

  static Future<List<Gift>> getGifts() async {
    String _params = await HTTPRequest.getQueryString();

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Gift/getGift?' +
        _params);

    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);

      List _rawGift = dataObject['gift'];
      List<Gift> _gift = _rawGift.map((giftItem) => Gift.fromJson(giftItem)).toList();

      return _gift;
    }

    return [];
  }
  static Future<Gift?> getGift(String idGift) async {
    String _params = await HTTPRequest.getQueryString();

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Gift/getGift/'+idGift+'?' +
        _params);

    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);

      dynamic _rawGift = dataObject['gift'];
      if(_rawGift != false){
        Gift _gift = Gift.fromJson(_rawGift);

        return _gift;
      }
    }

    return null;
  }
}
