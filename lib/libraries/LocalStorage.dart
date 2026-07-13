import 'dart:convert';

import 'package:package_info/package_info.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

class LocalStorage {
  static Future<bool> isAlamatDeliveryDanJadwalDeliverySet() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    bool isAlamatDeliveryDanJadwalDeliverySetVariable =
        sharedPreferences.getBool('isAlamatDeliveryDanJadwalDeliverySet') ??
            false;

    return isAlamatDeliveryDanJadwalDeliverySetVariable;
  }

  static Future<Map<String, String?>> getSavedLatitudeAndLongitude() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    String? latitude = sharedPreferences.getString('savedLatitude');
    String? longitude = sharedPreferences.getString('savedLongitude');
    String? jadwalDeliveryCode =
        sharedPreferences.getString('savedJadwalDeliveryCode');
    String? alamat = sharedPreferences.getString('savedAlamat');

    Map<String, String?> savedLatitudeAndLongitude = {
      'latitude': latitude,
      'longitude': longitude,
      'jadwalDeliveryCode': jadwalDeliveryCode,
      'alamat': alamat
    };

    return savedLatitudeAndLongitude;
  }

  static Future<String> getGoogleMapsApiKey() async {
    return 'AIzaSyDgVwDoS4Pbz6bogaTixjani68t1AKuUd8';
  }

  static Future<String> getAuthToken() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.getString('authToken') ?? '';
  }

  static Future<String> getCurrentUserID() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.getString('idUser') ?? '';
  }

  static Future<String> getCartCode() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    String? cartCode = sharedPreferences.getString('cartCode');
    if (cartCode == null) {
      DateTime currentTime = DateTime.now();
      // String tanggal = currentTime.day.toString();
      // String bulan = currentTime.month.toString();
      // String tahun = currentTime.year.toString();
      int jam = currentTime.hour;
      int menit = currentTime.minute;
      int detik = currentTime.second;
      int mili = currentTime.millisecond;

      String jamString = jam.toString();
      if (jam <= 9) {
        jamString = '0' + jamString;
      }
      String menitString = menit.toString();
      if (menit <= 9) {
        menitString = '0' + menitString;
      }
      String detikString = detik.toString();
      if (detik <= 9) {
        detikString = '0' + detikString;
      }
      String miliString = mili.toString();
      if (mili <= 9) {
        miliString = '0' + miliString;
      }

      cartCode = '$jamString$menitString$detikString$miliString';

      sharedPreferences.setString('cartCode', cartCode);
    }
    return cartCode;
  }

  static Future<void> clearCartCode() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    sharedPreferences.remove('cartCode');
  }

  static Future<String?> getUsedVoucher() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.getString('usedVoucher');
  }

  static setUsedVoucher(String idVoucher) async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.setString('usedVoucher', idVoucher);
  }

  static Future<void> clearUsedVoucher() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    sharedPreferences.remove('usedVoucher');
  }

  static Future<String> getStatusVoucher(String idVoucher) async {
    String authToken = await getAuthToken();

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Voucher/getStatusVoucher/$idVoucher?authToken=$authToken');
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      String statusVoucher = dataObject['statusVoucher'];

      return statusVoucher;
    }
    return 'kadaluarsa';
  }

  static Future<void> setExternalUserID(String externalUserID) async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    sharedPreferences.setString('externalUserID', externalUserID);
  }

  static getExternalUserID() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    return sharedPreferences.getString('externalUserID');
  }

  static Future<void> removeExternalUserID() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    sharedPreferences.remove('externalUserID');
  }

  static Future<int> getIdPenjualFromActiveUser() async {
    String authToken = await LocalStorage.getAuthToken();

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/User/getDetailUser?getUserEntities=id&authToken=$authToken');
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      dynamic detailUser = dataObject['detailUser'];
      String idUser = (detailUser != false) ? detailUser['id'] : '0';
      return int.parse(idUser);
    }

    return 0;
  }

  static Future<String> getApplicationID() async {
    return 'id.laukpauk';
  }

  static Future<bool> isUserVerified() async {
    String authToken = await getAuthToken();

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Auth/cekVerifikasiUser?authToken=$authToken');
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool isVerified = dataObject['isVerified'];

      return isVerified;
    }
    return false;
  }

  static Future<Map<String, dynamic>?> savedJadwalDeliveryMap() async {
    SharedPreferences sharedPreferences = await SharedPreferences.getInstance();
    String? _encodedSavedJadwalDeliveryMap =
        sharedPreferences.getString('savedJadwalDeliveryMap');

    if (_encodedSavedJadwalDeliveryMap != null) {
      Map<String, dynamic> decodedSavedJadwalDeliveryMap =
          jsonDecode(_encodedSavedJadwalDeliveryMap);

      return decodedSavedJadwalDeliveryMap;
    }
    return null;
  }

  static Future<void> lewatiOTPRegistrasi() async {
    SharedPreferences _sharedPreferences =
        await SharedPreferences.getInstance();
    _sharedPreferences.setBool('lewatiOTPRegistrasi', true);
  }

  static Future<bool> isOTPRegistrasiPassed() async {
    SharedPreferences _sharedPreferences =
        await SharedPreferences.getInstance();
    bool? _isPassed = _sharedPreferences.getBool('lewatiOTPRegistrasi') ?? null;

    return (_isPassed != null) ? _isPassed : false;
  }

  static Future<String> getBuildNumber() async {
    PackageInfo packageInfo = await PackageInfo.fromPlatform();

    String _buildNumber = packageInfo.buildNumber;

    return _buildNumber;
  }
  static Future<void> setPasarPalingDekat(Map<String, dynamic> pasarPalingDekat) async{
    SharedPreferences _sharedPreferences = await SharedPreferences.getInstance();

        String _pasarPalingDekatMapString = jsonEncode(pasarPalingDekat);
        _sharedPreferences.setString('pasarPalingDekat', _pasarPalingDekatMapString);
  }
  static Future<Map<String, dynamic>?> getPasarPalingDekat() async {
    SharedPreferences _sharedPreferences = await SharedPreferences.getInstance();

        String? _pasarPalingDekatMapString = _sharedPreferences.getString('pasarPalingDekat');

        Map<String, dynamic>? _pasarPalingDekat;
        if(_pasarPalingDekatMapString != null){
          _pasarPalingDekat = jsonDecode(_pasarPalingDekatMapString);
        }
        return _pasarPalingDekat;
  }
}
