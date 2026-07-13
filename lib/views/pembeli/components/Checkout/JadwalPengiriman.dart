import 'dart:convert';

import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/views/SplashScreen.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/models/ADJDModel.dart';

import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:shared_preferences/shared_preferences.dart';

class JadwalPengiriman extends StatefulWidget {
  final Function(Map<String, dynamic>? jadwalPengiriman)
      jadwalPengirimanTerpilih;
  JadwalPengiriman({required this.jadwalPengirimanTerpilih});
  _JadwalPengirimanState createState() => _JadwalPengirimanState();
}

class _JadwalPengirimanState extends State<JadwalPengiriman> {
  bool _readyToShow = true;

  Future<Map<String, dynamic>?> getJadwalDeliverySelected() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    String? savedJadwalDelivery =
        _sP.getString('savedJadwalDeliveryMap') ?? null;

    if (savedJadwalDelivery != null) {
      Map<String, dynamic> savedJadwalDeliveryMap =
          jsonDecode(savedJadwalDelivery);

      return savedJadwalDeliveryMap;
    }
    return null;
  }

  void initJadwalPengirimanToParent() async {
    Map<String, dynamic>? savedJadwalDelivery =
        await getJadwalDeliverySelected();
    widget.jadwalPengirimanTerpilih(savedJadwalDelivery);
  }

  void initState() {
    super.initState();
    initJadwalPengirimanToParent();
  }

  Widget build(BuildContext context) {
    return Consumer<HeaderViewModel>(builder: (context, _headerViewModel, _) {
      return Container(
          width: SizeConfig.horizontalBlock * 100.0,
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('Jadwal Pengiriman',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 4,
                        fontWeight: FontWeight.w700)),
                GestureDetector(
                  onTap: () async {
                    String _title = 'Ubah Alamat dan Jadwal';
                    String _subTitle =
                        'Jika anda mengganti alamat / jam delivery, seluruh pesanan di keranjang akan terhapus otomatis.';
                    Widget _actions = Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          CustomButton.button(context, 'Batal', () {
                            Navigator.pop(context, false);
                          }, color: CustomColors.textMuted, outline: true),
                          SizedBox(width: 5.0),
                          CustomButton.button(context, 'Lanjutkan', () {
                            Navigator.pop(context, true);
                          }, color: CustomColors.green)
                        ]);
                    bool? _konfirmasi = await CustomAlert.textComponent(context,
                        title: _title, subTitle: _subTitle, actions: _actions);

                    if (_konfirmasi != null && _konfirmasi == true) {
                      setState(() {
                        _readyToShow = false;
                      });
                      await ADJDModel.deleteAlamatDanJadwalDelivery();
                      Map<String, dynamic> _newADJD =
                          await HeaderModel.adjd(context);

                      String? _latitude = _newADJD['latitude'];
                      String? _longitude = _newADJD['longitude'];
                      String? _kodeJadwalDelivery =
                          _newADJD['kodeJadwalDelivery'];
                      setState(() {
                        _readyToShow = true;
                      });
                      if (_latitude != null &&
                          _longitude != null &&
                          _kodeJadwalDelivery != null) {
                        Provider.of<HeaderViewModel>(context, listen: false)
                            .isADJDSet = true;
                        Navigator.pushAndRemoveUntil(
                            context,
                            MaterialPageRoute(builder: (_) => SplashScreen()),
                            (route) => false);
                      }
                    }
                  },
                  child: Text('Ubah Alamat dan Jadwal',
                      style: TextStyle(
                          color: CustomColors.green,
                          fontSize: SizeConfig.horizontalBlock * 3.0,
                          fontWeight: FontWeight.w700)),
                ),
              ],
            ),
            SizedBox(height: 10.0),
            (_readyToShow)
                ? Column(children: [
                    FutureBuilder<Map<String, dynamic>?>(
                        future: getJadwalDeliverySelected(),
                        builder: (context, snapshot) {
                          if (snapshot.hasData) {
                            Map<String, dynamic>? savedJadwalDeliveryMap =
                                snapshot.data;

                            return (savedJadwalDeliveryMap == null)
                                ? SizedBox()
                                : Container(
                                    width: SizeConfig.horizontalBlock * 100.0,
                                    decoration: BoxDecoration(
                                        color: Color(0x35FFc107),
                                        borderRadius:
                                            BorderRadius.circular(10.0)),
                                    padding: EdgeInsets.symmetric(
                                        horizontal: 15.0, vertical: 10.0),
                                    child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                              'Pengantaran ' +
                                                  savedJadwalDeliveryMap[
                                                      'day'] +
                                                  ' ' +
                                                  savedJadwalDeliveryMap[
                                                      'sessi'],
                                              style: TextStyle(
                                                  color: CustomColors.grey,
                                                  fontWeight: FontWeight.bold,
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.5)),
                                          SizedBox(height: 3.0),
                                          Text(
                                              ' Estimasi Jam ' +
                                                  savedJadwalDeliveryMap[
                                                      'rentangWaktu'] +
                                                  ' WIB',
                                              style: TextStyle(
                                                  fontStyle: FontStyle.italic,
                                                  color: CustomColors.grey,
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      2.75))
                                        ]));
                          } else {
                            return Padding(
                                padding: EdgeInsets.symmetric(vertical: 15),
                                child: Center(
                                    child: SizedBox(
                                        height: 15,
                                        width: 15,
                                        child: CircularProgressIndicator(
                                            strokeWidth: 1.5))));
                          }
                        }),
                    SizedBox(height: 5.0),
                    FutureBuilder<Map<String, dynamic>>(
                      future: LocalStorage.getSavedLatitudeAndLongitude(),
                      builder: (context, snapshot) {
                        if (snapshot.hasData) {
                          Map<String, dynamic>? savedLatLong = snapshot.data;

                          String? _alamat;
                          if (savedLatLong != null) {
                            _alamat = savedLatLong['alamat'];
                          }

                          return (savedLatLong == null)
                              ? SizedBox()
                              : Container(
                                  width: SizeConfig.horizontalBlock * 100.0,
                                  decoration: BoxDecoration(
                                      color: Color(0x3517a2b8),
                                      borderRadius:
                                          BorderRadius.circular(10.0)),
                                  padding: EdgeInsets.symmetric(
                                      horizontal: 15.0, vertical: 10.0),
                                  child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text('Alamat Pengantaran',
                                            style: TextStyle(
                                                color: CustomColors.grey,
                                                fontWeight: FontWeight.bold,
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3.5)),
                                        SizedBox(height: 3.0),
                                        (_alamat != null)
                                            ? Text(_alamat,
                                                style: TextStyle(
                                                    color: CustomColors.grey,
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        2.75))
                                            : SizedBox()
                                      ]));
                        } else {
                          return Padding(
                              padding: EdgeInsets.symmetric(vertical: 15),
                              child: Center(
                                  child: SizedBox(
                                      height: 15,
                                      width: 15,
                                      child: CircularProgressIndicator(
                                          strokeWidth: 1.5))));
                        }
                      },
                    )
                  ])
                : Padding(
                    padding: EdgeInsets.symmetric(
                        vertical: SizeConfig.horizontalBlock * 4.5),
                    child: Center(
                        child: SizedBox(
                            width: SizeConfig.horizontalBlock * 4.0,
                            height: SizeConfig.horizontalBlock * 4.0,
                            child:
                                CircularProgressIndicator(strokeWidth: 1.5))),
                  )
          ]));
    });
  }
}
