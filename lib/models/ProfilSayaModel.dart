import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class ProfilSayaModel {
  static Future<dynamic> getDetailActiveUser({String? select}) async {
    String selectQS = (select != null) ? 'getUserEntities=$select' : '';

    String _params =
        await HTTPRequest.getQueryString(params: selectQS, authToken: true);
    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getDetailUser?' + _params);

    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      dynamic detailUser = dataObject['detailUser'];
      return detailUser;
    }

    return false;
  }

  static Future<bool> simpanPerubahanDataProfil(
      Map<String, dynamic> dataPerubahan) async {
    dataPerubahan = await HTTPRequest.postBody(body: dataPerubahan);
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/updateProfil?' + _params);
    Response httpResponse = await post(_url, body: dataPerubahan);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusUpdateProfil = dataObject['statusUpdateProfil'];
      return statusUpdateProfil;
    }

    return false;
  }
}
