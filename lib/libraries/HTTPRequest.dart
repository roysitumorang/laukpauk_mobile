import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/models/AuthModel.dart';

class HTTPRequest {
  static const String _versionCodeKey = 'versionCode';

  static Future<Map<String, dynamic>> postBody(
      {Map<String, dynamic>? body}) async {
    String _versionCode = await LocalStorage.getBuildNumber(); //build number

    Map<String, dynamic> _returnedData = {_versionCodeKey: _versionCode};

    if (body != null) {
      _returnedData = {..._returnedData, ...body};
    }

    return _returnedData;
  }

  static Future<String> getQueryString(
      {String? params, bool authToken = false}) async {
    String _versionCode = await LocalStorage.getBuildNumber(); //build number

    String _returnedData = _versionCodeKey + '=' + _versionCode;

    if (authToken) {
      bool _isLoggedIn = await AuthModel.isLogin();
      String _authToken =
          (_isLoggedIn) ? await LocalStorage.getAuthToken() : '';

      _returnedData = _returnedData + '&authToken=' + _authToken;
    }

    if (params != null) {
      _returnedData = _returnedData + '&' + params;
    }
    return _returnedData;
  }
}
