import 'dart:convert';

import 'package:geolocator/geolocator.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/TanyaJadwal.dart';

import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ADJDModel {
  static String defaultLatitude = '3.5889242';
  static String defaultLongitude = '98.6790958';
  static String defaultAlamat = 'Merdeka Walk Medan';

  static Future<Map<String, dynamic>?> detectMyLocation(
      BuildContext context) async {
    LocationPermission? _permissionRequest = LocationPermission.always;

    LocationPermission _permission = await Geolocator.checkPermission();
    if (_permission == LocationPermission.denied ||
        _permission == LocationPermission.deniedForever) {
      if (_permission == LocationPermission.denied) {
        LocationPermission _requestPermission =
            await Geolocator.requestPermission();
        if (_requestPermission == LocationPermission.denied) {
          _permissionRequest = null;
        }
      }
      _permissionRequest = null;
    }

    try {
      Position myLocation = await Geolocator.getCurrentPosition(
          desiredAccuracy: LocationAccuracy.bestForNavigation);

      String latitude = myLocation.latitude.toString();
      String longitude = myLocation.longitude.toString();

      Map<String, dynamic> myLocationMap = {
        'permissionRequest': _permissionRequest,
        'latitude': latitude,
        'longitude': longitude
      };

      return myLocationMap;
    } catch (e) {
      print('error detect my location');
      print(e);

      return null;
    }
  }

  static askAlamatPengantaran(BuildContext context,
      {String latitude = '',
      String longitude = '',
      String? title,
      String? content}) async {
    title = (title == null) ? 'Alamat Pengantaran' : title;
    content = (content == null)
        ? 'Apakah belanjaan akan diantarkan ke alamat tempat anda berada sekarang ?'
        : content;

    await Future.delayed(Duration(milliseconds: 1));
    String geocodingApiKey = await LocalStorage.getGoogleMapsApiKey();

    Uri _url = Uri.parse(
        'https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$geocodingApiKey');
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      String formattedAddress = (dataObject['results'].length >= 1)
          ? dataObject['results'][0]['formatted_address']
          : 'Alamat Tidak diketahui';

      Widget _actions =
          Row(mainAxisAlignment: MainAxisAlignment.center, children: [
        TextButton(
            onPressed: () {
              Navigator.pop(context);
            },
            child: Text('Tutup')),
        TextButton(
            onPressed: () {
              Navigator.pop(
                  context, {'answer': 'tidak', 'address': formattedAddress});
            },
            child: Text('Alamat Lain')),
        TextButton(
            onPressed: () {
              Navigator.pop(
                  context, {'answer': 'ya', 'address': formattedAddress});
            },
            child: Text('Ya'))
      ]);
      Widget _moreWidgets = Text(formattedAddress,
          textAlign: TextAlign.center,
          style: TextStyle(
              fontSize: SizeConfig.horizontalBlock * 3.25,
              color: CustomColors.textMuted));

      return CustomAlert.textComponent(context,
          title: title,
          subTitle: content,
          actions: _actions,
          moreWidgets: _moreWidgets);
    } else {
      showDialog(
          context: context,
          builder: (context) => AlertDialog(
                  actions: [
                    TextButton(
                        onPressed: () {
                          Navigator.pop(context);
                        },
                        child: Text('OK'))
                  ],
                  title: Text('Pendeteksian Lokasi'),
                  content: Text('Gagal mendeteksi lokasi anda saat ini !')));
    }
  }

  static askJadwalDelivery(BuildContext context,
      {latitude, longitude, currentLocation}) {
    String _title = 'Jadwal Delivery';
    String _subTitle = 'Silahkan pilih jadwal pengantaran yang anda inginkan :';
    return CustomAlert.textComponent(context,
        title: _title,
        subTitle: _subTitle,
        actions: null,
        moreWidgets: TanyaJadwal(currentLocation: currentLocation));
  }

  static reverseGeocoding({latitude, longitude}) async {
    String geocodingApiKey = await LocalStorage.getGoogleMapsApiKey();

    Uri _url = Uri.parse(
        'https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$geocodingApiKey');
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      String formattedAddress = (dataObject['results'].length >= 1)
          ? dataObject['results'][0]['formatted_address']
          : 'Alamat Tidak diketahui';

      return formattedAddress;
    }

    return 'Alamat Tidak Diketahui';
  }

  static Future<void> deleteAlamatDanJadwalDelivery() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    String? latitude = _sP.getString('savedLatitude');
    String? longitude = _sP.getString('savedLongitude');
    String? jadwalDeliveryCode = _sP.getString('savedJadwalDeliveryCode');
    String? jadwalDeliveryMap = _sP.getString('savedJadwalDeliveryMap');
    String? alamat = _sP.getString('savedAlamat');

    if (latitude != null) {
      _sP.setString('coppiedLatitude', latitude);
    }
    if (longitude != null) {
      _sP.setString('coppiedLongitude', longitude);
    }
    if (jadwalDeliveryCode != null) {
      _sP.setString('coppiedJadwalDeliveryCode', jadwalDeliveryCode);
    }
    if (jadwalDeliveryMap != null) {
      _sP.setString('coppiedJadwalDeliveryMap', jadwalDeliveryMap);
    }
    if (alamat != null) {
      _sP.setString('coppiedAlamat', alamat);
    }

    _sP.remove('isAlamatDeliveryDanJadwalDeliverySet');
    _sP.remove('savedLatitude');
    _sP.remove('savedLongitude');
    _sP.remove('savedJadwalDeliveryCode');
    _sP.remove('savedJadwalDeliveryMap');
    _sP.remove('savedAlamat');
  }

  static Future<Map<String, dynamic>> getCoppiedADJD() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    String? coppiedLatitude = _sP.getString('coppiedLatitude');
    String? coppiedLongitude = _sP.getString('coppiedLongitude');
    String? coppiedJadwalDeliveryCode =
        _sP.getString('coppiedJadwalDeliveryCode');
    String? coppiedJadwalDeliveryMap =
        _sP.getString('coppiedJadwalDeliveryMap');
    String? coppiedAlamat = _sP.getString('coppiedAlamat');

    Map<String, dynamic> coppiedADJD = {
      'coppiedLatitude': coppiedLatitude,
      'coppiedLongitude': coppiedLongitude,
      'coppiedJadwalDeliveryCode': coppiedJadwalDeliveryCode,
      'coppiedJadwalDeliveryMap': coppiedJadwalDeliveryMap,
      'coppiedAlamat': coppiedAlamat
    };

    return coppiedADJD;
  }

  static Future<void> deleteCoppiedADJD() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    _sP.remove('coppiedLatitude');
    _sP.remove('coppiedLongitude');
    _sP.remove('coppiedJadwalDeliveryCode');
    _sP.remove('coppiedJadwalDeliveryMap');
    _sP.remove('coppiedAlamat');
  }
}
