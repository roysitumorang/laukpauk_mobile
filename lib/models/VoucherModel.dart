import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class VoucherModel {
  static Future<dynamic> getVoucherDetail(String idVoucher) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Voucher/getVoucher/$idVoucher?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);

      return dataObject['dataVoucher'];
    }

    return false;
  }
}
