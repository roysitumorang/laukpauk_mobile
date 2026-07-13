import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

class HistoriPencarianModel {
  static Future<List> getHistoriPencarian() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    String? _encodedHistoriPencarian =
        _sP.getString('historiPencarian') ?? null;
    if (_encodedHistoriPencarian != null) {
      List _historiPencarian = jsonDecode(_encodedHistoriPencarian);
      return _historiPencarian;
    }

    return [];
  }

  static Future<bool> addHistoriPencarian(String search) async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    String? _encodedHistoriPencarian =
        _sP.getString('historiPencarian') ?? null;

    DateTime _now = DateTime.now();

    String _jam = _now.hour.toString();
    String _menit = _now.minute.toString();
    String _detik = _now.second.toString();

    String _tanggal = _now.day.toString();
    String _bulan = _now.month.toString();
    String _tahun = _now.year.toString();

    String _onTime = _jam +
        ':' +
        _menit +
        ':' +
        _detik +
        ' ' +
        _tanggal +
        '/' +
        _bulan +
        '/' +
        _tahun;

    if (_encodedHistoriPencarian == null) {
      _encodedHistoriPencarian = jsonEncode([
        {'title': search, 'onTime': _onTime}
      ]);
    } else {
      List _decodedHistoriPencarian = jsonDecode(_encodedHistoriPencarian);
      _decodedHistoriPencarian.add({'title': search, 'onTime': _onTime});
      _encodedHistoriPencarian = jsonEncode(_decodedHistoriPencarian);
    }

    _sP.setString('historiPencarian', _encodedHistoriPencarian);

    return true;
  }
}
