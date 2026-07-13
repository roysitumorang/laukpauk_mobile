import 'dart:convert';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class HomepageModel {
  static Future<List> getArtikel() async {
    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Artikel/getArtikel');
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      String response = httpResponse.body;
      dynamic dataArtikelObject = json.decode(response);
      List dataArtikel = dataArtikelObject['listArtikel'];

      return dataArtikel;
    }
    return [];
  }

  static Future<int> getJumlahVoucher() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getJumlahVoucher?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);

      return dataObject['jumlahVoucher'];
    }

    return 0;
  }

  static Future<int> getTotalDompet() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getTotalDompet?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      return dataObject['totalDompet'];
    }

    return 0;
  }

  static Future<List> getDataPasar({required bool isADJDSet, bool showAll = false, int limitPasar = 2, String? pencarian, Map<String, dynamic>? savedADJD}) async {
String limitPasarQS = (showAll) ? '' : 'limitPasar=$limitPasar';
      String namaPasarQS = '';
      
      if(pencarian != null){
        if(pencarian.length >= 1){
          
        namaPasarQS   = 'namaPasar=$pencarian';
        }
      }

      String url =
          ServerPath.apiBaseURL + '/Pasar/getPasar?$limitPasarQS&$namaPasarQS';

          print(url);
      String _keyOfData = 'dataPasar';

      if (isADJDSet) {

        String? adjdLatitude;
        String? adjdLongitude;
        String? adjdKodeJadwalDelivery;

        if (savedADJD != null) {
          adjdLatitude = savedADJD['latitude'];
          adjdLongitude = savedADJD['longitude'];
          adjdKodeJadwalDelivery = savedADJD['jadwalDeliveryCode'];
        }

        if (adjdLatitude != null &&
            adjdLongitude != null &&
            adjdKodeJadwalDelivery != null) {
          url = ServerPath.apiBaseURL +
              '/Pasar/getPasarAroundOfThisLatLong?latitude=$adjdLatitude&longitude=$adjdLongitude&jadwalDeliveryCode=$adjdKodeJadwalDelivery&$limitPasarQS&$namaPasarQS';
          _keyOfData = 'listPasar';
        }
      }

      Uri _url = Uri.parse(url);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      List pasar = dataObject[_keyOfData];

      return pasar;
    }

    return [];
  }
  
}
