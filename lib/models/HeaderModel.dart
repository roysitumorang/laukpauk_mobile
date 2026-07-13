import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/models/CheckoutModel.dart';
import 'package:laukpauk/models/TransaksiModel.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/models/ADJDModel.dart';

import 'package:laukpauk/models/NotifikasiModel.dart';
import 'package:laukpauk/views/pembeli/PinGMAPS.dart';

import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:laukpauk/element-class/Gift.dart';
import 'package:laukpauk/models/GiftModel.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/GiftAlert.dart';

class HeaderModel {
  static Future<int> getJumlahCart() async {
    String cartCode = await LocalStorage.getCartCode();
    String _params =
        await HTTPRequest.getQueryString(params: 'cartCode=$cartCode');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getJumlahCart?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      int jumlahKeranjang = dataObject['jumlahKeranjang'];

      return jumlahKeranjang;
    }
    return 0;
  }

  static Future<int> getJumlahNotifikasi() async {
    int jumlahNotifikasiVariable =
        await NotifikasiModel.getJumlahNotifikasi('unread');
    int jumlahNotifikasiKhususVariable =
        await NotifikasiModel.getJumlahNotifikasiKhusus('unread');
    int jumlahSeluruhNotifikasi =
        jumlahNotifikasiVariable + jumlahNotifikasiKhususVariable;

    return jumlahSeluruhNotifikasi;
  }

  static Future<bool?> showAlamatPengantaran(
      BuildContext context, String alamatPengantaran) async {
    SizeConfig().initSize(context);

    String _title = 'Alamat Pengantaran';
    String _subTitle =
        'Alamat pengantaran yang anda tentukan berada di sekitar : ';

    Widget _moreWidgets = Text(alamatPengantaran,
        textAlign: TextAlign.center,
        style: TextStyle(
            fontSize: SizeConfig.horizontalBlock * 3.25,
            color: CustomColors.textMuted));

    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      TextButton(
          onPressed: () {
            Navigator.pop(context, false);
          },
          child: Text('Tutup')),
      TextButton(
          onPressed: () async {
            await TransaksiModel.removeTransactionSession(context);
            Navigator.pop(context, false);
          },
          child: Text('Reset')),
      TextButton(
          onPressed: () {
            Navigator.pop(context, true);
          },
          child: Text('Ubah'))
    ]);
    return CustomAlert.textComponent<bool>(context,
        title: _title,
        subTitle: _subTitle,
        actions: _actions,
        moreWidgets: _moreWidgets);
  }

  static Future<Map<String, dynamic>> adjd(BuildContext context,
      {void Function(Map<String, dynamic> returnedData)? callback,
      void Function()? nantiSajaCallback,
      void Function()? okSajaCallback}) async {
    String? readAbleSelectedLocation;
    String? latitude, longitude;
    String? kodeJadwalDeliverySelected;
    Map<String, dynamic>? jadwalDeliveryTerpilihMap;

    bool isLocationAndTimeOfDeliverySet =
        await LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
    print((isLocationAndTimeOfDeliverySet) ? 'set' : 'unset');

    if (!isLocationAndTimeOfDeliverySet) {
      bool adjdSelectionCompleted = false;
      Map<String, dynamic>? myLocation =
          await ADJDModel.detectMyLocation(context);

      if (myLocation != null) {
        latitude = myLocation['latitude'];
        longitude = myLocation['longitude'];

        Map<String, dynamic>? diantarKeLokasiSekarangMap =
            await ADJDModel.askAlamatPengantaran(context,
                latitude: latitude!, longitude: longitude!);

        if (diantarKeLokasiSekarangMap != null) {
          readAbleSelectedLocation = diantarKeLokasiSekarangMap['address'];
          String diantarKeLokasiSekarang = diantarKeLokasiSekarangMap['answer'];
          if (diantarKeLokasiSekarang == 'tidak') {
            Map<String, dynamic>? returnedDataFromPinGMAPS =
                await Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (_) => PinGMAPS(
                            currentLatitude: latitude!,
                            currentLongitude: longitude!)));

            if (returnedDataFromPinGMAPS != null) {
              latitude = returnedDataFromPinGMAPS['lat'].toString();
              longitude = returnedDataFromPinGMAPS['lng'].toString();
              readAbleSelectedLocation =
                  returnedDataFromPinGMAPS['formattedAddress'];
            }
          }

          Map<String, dynamic>? jadwalDeliveryMap =
              await ADJDModel.askJadwalDelivery(context,
                  latitude: latitude,
                  longitude: longitude,
                  currentLocation: readAbleSelectedLocation);

          if (jadwalDeliveryMap != null) {
            kodeJadwalDeliverySelected =
                jadwalDeliveryMap['jadwalDeliverySelected'];
            jadwalDeliveryTerpilihMap =
                jadwalDeliveryMap['jadwalDeliveryTerpilih'];
            if (kodeJadwalDeliverySelected != null &&
                jadwalDeliveryTerpilihMap != null) {
              adjdSelectionCompleted = true;
            }
          }
        }

        await setNewADJD(context, adjdSelectionCompleted,
            latitude: latitude,
            longitude: longitude,
            jadwalDeliveryCode: kodeJadwalDeliverySelected,
            jadwalDeliveryMap: jsonEncode(jadwalDeliveryTerpilihMap),
            alamat: readAbleSelectedLocation);
      } else {
        String _title = 'Laukpauk';
        String _subTitle =
            'Supaya mamak bisa belanja, aktifkan GPSnya dulu ya mak :)';

        Widget _actions = Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <TextButton>[
              TextButton(
                  onPressed: () {
                    Navigator.pop(context);
                  },
                  child: Text('OK'))
            ]);

        await CustomAlert.textComponent(context,
            title: _title, subTitle: _subTitle, actions: _actions);
      }
    } else {
      Map<String, dynamic> getSavedLatitudeAndLongitudeMap =
          await LocalStorage.getSavedLatitudeAndLongitude();

      latitude = getSavedLatitudeAndLongitudeMap['latitude'];
      longitude = getSavedLatitudeAndLongitudeMap['longitude'];
      kodeJadwalDeliverySelected =
          getSavedLatitudeAndLongitudeMap['jadwalDeliveryCode'];
    }

    Map<String, dynamic> adjdSet = {
      'latitude': latitude,
      'longitude': longitude,
      'kodeJadwalDelivery': kodeJadwalDeliverySelected
    };

    return adjdSet;
  }

  static Future<void> setNewADJD(
      BuildContext context, bool isADJDSelectionCompleted,
      {required String? latitude,
      required String? longitude,
      required String? jadwalDeliveryCode,
      required String? alamat,
      required String? jadwalDeliveryMap}) async {
    bool clearKeranjang = (isADJDSelectionCompleted) ? true : false;
    bool isAlamatDeliveryDanJadwalDeliverySet;

    Map<String, dynamic> coppiedADJD = await ADJDModel.getCoppiedADJD();

    String? coppiedLatitude = coppiedADJD['coppiedLatitude'];
    String? coppiedLongitude = coppiedADJD['coppiedLongitude'];
    String? coppiedJadwalDeliveryCode =
        coppiedADJD['coppiedJadwalDeliveryCode'];
    String? coppiedAlamat = coppiedADJD['coppiedAlamat'];
    String? coppiedJadwalDeliveryMap = coppiedADJD['coppiedJadwalDeliveryMap'];

    bool isCoppiedADJDThereIsNotNull = (coppiedLatitude != null &&
        coppiedLongitude != null &&
        coppiedJadwalDeliveryCode != null &&
        coppiedAlamat != null &&
        coppiedJadwalDeliveryMap != null);

    if (isADJDSelectionCompleted) {
      isAlamatDeliveryDanJadwalDeliverySet = true;
    } else {
      // cek dulu coppied adjdnya, kalau tidak ada yang null, maka set true, else set false

      isAlamatDeliveryDanJadwalDeliverySet = isCoppiedADJDThereIsNotNull;
    }

    if (!isADJDSelectionCompleted) {
      latitude = coppiedLatitude;
      longitude = coppiedLongitude;
      jadwalDeliveryCode = coppiedJadwalDeliveryCode;
      alamat = coppiedAlamat;
      jadwalDeliveryMap = coppiedJadwalDeliveryMap;
      if (isCoppiedADJDThereIsNotNull) {
        isADJDSelectionCompleted = true;
      }
      //khusus untuk jadwal delivery yang gak lengkap, jadi nanti klo gak lengkap maka dicopy dari adjd yang dicpoy sebelum dihapus, nah dari adjd yang dicopy tsb akan diset kembali jika adjd tidak lengkap
    }

    if (isADJDSelectionCompleted) {
      SharedPreferences _sP = await SharedPreferences.getInstance();

      _sP.setBool('isAlamatDeliveryDanJadwalDeliverySet',
          isAlamatDeliveryDanJadwalDeliverySet);

      if (latitude != null) {
        _sP.setString('savedLatitude', latitude);
      }
      if (longitude != null) {
        _sP.setString('savedLongitude', longitude);
      }
      if (jadwalDeliveryCode != null) {
        _sP.setString('savedJadwalDeliveryCode', jadwalDeliveryCode);
      }
      if (jadwalDeliveryMap != null) {
        DateTime _now = DateTime.now();

        String _tahun = _now.year.toString();
        String _bulan = _now.month.toString();
        String _tanggal = _now.day.toString();

        String _jam = _now.hour.toString();
        String _menit = _now.minute.toString();
        String _detik = _now.second.toString();

        String _currentDateTime = _tahun +
            '-' +
            _bulan +
            '-' +
            _tanggal +
            ' ' +
            _jam +
            ':' +
            _menit +
            ':' +
            _detik;

        Map<String, dynamic> _jadwalDelivery = jsonDecode(jadwalDeliveryMap);
        _jadwalDelivery['setOn'] = _currentDateTime;

        jadwalDeliveryMap = jsonEncode(_jadwalDelivery);

        print(jadwalDeliveryMap);
        _sP.setString('savedJadwalDeliveryMap', jadwalDeliveryMap);
      }
      if (alamat != null) {
        _sP.setString('savedAlamat', alamat);
      }

      Map<String, dynamic> savedADJD =
          await LocalStorage.getSavedLatitudeAndLongitude();

      Provider.of<HeaderViewModel>(context, listen: false).isADJDSet =
          isAlamatDeliveryDanJadwalDeliverySet;
      Provider.of<HeaderViewModel>(context, listen: false).alamatDelivery =
          alamat ?? '';
      Provider.of<HeaderViewModel>(context, listen: false).savedADJD =
          savedADJD;

      if (clearKeranjang) {
        await CheckoutModel.bersihkanKeranjang();
        Provider.of<HeaderViewModel>(context, listen: false).jumlahCart = 0;
      }
    }
  }

  static Future<void> makeSureADJD(BuildContext context) async {
    await Future.delayed(Duration(seconds: 3));
    bool show = await showMakeSure();
    if (show) {
      SharedPreferences _sP = await SharedPreferences.getInstance();

      String _subTitle =
          'Mak, pastikan lokasi pengantaran sebelum belanjaannya diantar ya...';
      Widget _actions = CustomButton.button(context, 'OK', () {
        _sP.setBool('showMakeSureADJD', false);

        Navigator.pop(context);
      }, color: CustomColors.green);

      await CustomAlert.textComponent(context,
          title: 'Laukpauk', subTitle: _subTitle, actions: _actions);
    }
  }

static Future<void> _showGift(BuildContext context, List<Gift> gifts, void Function() onClose) async {
    if (gifts.length >= 1) {
      SharedPreferences _sP = await SharedPreferences.getInstance();
      await showDialog(
          context: context,
          builder: (context) => GiftAlert(gifts: gifts, onClose: (() {
            _sP.setBool('showAlertGift', false);
            onClose();
          })));
    }
  }

  static Future<void> giftChecking(BuildContext context) async {
    await Future.delayed(Duration(seconds: 3));
    bool show = await showAlertGift();
    if (show) {
      List<Gift> _gift = await GiftModel.getGifts();
      await _showGift(context, _gift, (){
        Navigator.pop(context);
      });
    }
  }

  static Future<bool> showMakeSure() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    bool showMakeSureVariable = _sP.getBool('showMakeSureADJD') ?? true;

    return showMakeSureVariable;
  }

  static Future<bool> showAlertGift() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    bool showAlertGiftVariable = _sP.getBool('showAlertGift') ?? true;
    print('showAlertGift');
    print(showAlertGiftVariable);

    return showAlertGiftVariable;
  }


  static Future<void> alertADJDKadaluarsa(BuildContext context) async {
    Widget _icon =
        Image.asset(ClientPath.assetsImg + '/laukpauk-icon.png', width: 50.0);
    String _subTitle =
        'Mak, jadwal pengantaran yang mamak pilih sudah kadaluarsa. Keranjang belanja dikosongkan dan mamak harus tentukan alamat dan jadwal pengantaran lagi ya mak.....';
    Widget _actions = CustomButton.button(context, 'OK', () {
      Navigator.pop(context);
    }, color: CustomColors.green);

    await CustomAlert.textComponent(context,
        icon: _icon,
        title: 'Jadwal Pengantaran Kadaluarsa',
        subTitle: _subTitle,
        actions: _actions);
  }
}
