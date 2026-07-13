import 'package:flutter/foundation.dart';
import 'package:laukpauk/models/ADJDModel.dart';

import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/models/AuthModel.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

class HeaderViewModel with ChangeNotifier {
  HeaderViewModel() {
    HeaderModel.getJumlahCart().then((newValue) => _jumlahCart = newValue);
    AuthModel.isLogin().then((isLogin) {
      if (isLogin) {
        HeaderModel.getJumlahNotifikasi()
            .then((newValue) => _jumlahNotifikasi = newValue);
      } else {
        _jumlahNotifikasi = 0;
      }
    });
    LocalStorage.isAlamatDeliveryDanJadwalDeliverySet().then((newValue) {
      _isADJDSet = newValue;
      if (newValue) {
        LocalStorage.getSavedLatitudeAndLongitude().then((savedADJD) {
          String savedAlamat = savedADJD['alamat'] ?? ADJDModel.defaultAlamat;
          _alamatDelivery = savedAlamat;
          _savedADJD = savedADJD;
        });
      }
    });
  }

  int _jumlahCart = 0;
  int _jumlahNotifikasi = 0;
  bool _isADJDSet = false;
  String _alamatDelivery = '';
  Map<String, dynamic>? _savedADJD;

  int get jumlahCart => _jumlahCart;
  set jumlahCart(int newValue) {
    _jumlahCart = newValue;
    notifyListeners();
  }

  int get jumlahNotifikasi => _jumlahNotifikasi;
  set jumlahNotifikasi(int newValue) {
    _jumlahNotifikasi = newValue;
    notifyListeners();
  }

  bool get isADJDSet => _isADJDSet;
  set isADJDSet(bool newValue) {
    _isADJDSet = newValue;
    notifyListeners();
  }

  String get alamatDelivery => _alamatDelivery;
  set alamatDelivery(String newValue) {
    _alamatDelivery = newValue;
    notifyListeners();
  }

  Map<String, dynamic>? get savedADJD => _savedADJD;
  set savedADJD(Map<String, dynamic>? newValue) {
    _savedADJD = newValue;
    notifyListeners();
  }
}
