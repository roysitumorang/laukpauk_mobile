import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class KategoriProdukModel {
  static Future<List> getKategoriProduk({int? limitData}) async {
    String limitDataQS = (limitData != null)
        ? 'limitKategoriProduk=${limitData.toString()}'
        : '';

    String _params = await HTTPRequest.getQueryString(params: limitDataQS);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/KategoriProduk/getKategoriProduk?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List kategoriProduk = dataObject['kategoriProduk'];

      return kategoriProduk;
    }
    return [];
  }

  static Future<List> getProdukInThisCategory(String idKategoriProduk,
      {String? select}) async {
    String selectQS = (select != null) ? 'select=$select' : '';
    String _params = await HTTPRequest.getQueryString(params: selectQS);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Produk/getProductsInThisCategory/$idKategoriProduk?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> returnedData = jsonDecode(httpResponse.body);
      List productInThisCategory = returnedData['productInThisCategory'];

      return productInThisCategory;
    }
    return [];
  }
}
