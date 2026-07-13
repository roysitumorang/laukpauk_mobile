import 'dart:convert';

import 'package:http/http.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

class AuthModel {
  static Future<bool> isLogin() async {
    bool isLogin = false;

    String authToken = await LocalStorage.getAuthToken();

    if (authToken.isNotEmpty) {
      Uri _url = Uri.parse(
          ServerPath.apiBaseURL + '/Auth/cekToken?authToken=$authToken');
      Response httpResponse = await get(_url);
      if (httpResponse.statusCode == 200) {
        Map dataObject = jsonDecode(httpResponse.body);
        isLogin = dataObject['cekToken'];
      }
    }

    return isLogin;
  }

  static Future<bool?> isOTPConfirmed() async {
    bool? _isOTPConfirmed;
    bool _isLogin = await isLogin();
    if (_isLogin) {
      String _authToken = await LocalStorage.getAuthToken();
      Uri _url = Uri.parse(
          ServerPath.apiBaseURL + '/Auth/isOTPConfirmed?authToken=$_authToken');
      Response _httpResponse = await get(_url);
      if (_httpResponse.statusCode == 200) {
        Map<String, dynamic> _decodedBody = jsonDecode(_httpResponse.body);
        _isOTPConfirmed = _decodedBody['isOTPConfirmed'];
      }
    }
    return _isOTPConfirmed;
  }
}
