import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/global-components/Pencarian.dart';

import 'package:laukpauk/views/pembeli/components/ProdukDiDalamPasar/PasarLainItem.dart';

class PasarLainLuarCA extends StatefulWidget {
  PasarLainLuarCA();
  _PasarLainLuarCAState createState() => _PasarLainLuarCAState();
}

class _PasarLainLuarCAState extends State<PasarLainLuarCA> {
  bool isReadyToShow = false;
  List pasarLainLuarCAState = [];
  List pasarLainLuarCAImmutableState = [];
  String pencarianString = '';

  Future<List> getPasarLainLuarCA() async {
    String latitude, longitude, jadwalDeliveryCode;

    bool isADJDSet = await LocalStorage.isAlamatDeliveryDanJadwalDeliverySet();
    if (isADJDSet) {
      Map<String, dynamic> savedADJD =
          await LocalStorage.getSavedLatitudeAndLongitude();
      latitude = savedADJD['latitude'];
      longitude = savedADJD['longitude'];
      jadwalDeliveryCode = savedADJD['jadwalDeliveryCode'];

      String _params = await HTTPRequest.getQueryString(
          params: 'latitude=' +
              latitude +
              '&longitude=' +
              longitude +
              '&jadwalDeliveryCode=$jadwalDeliveryCode');

      Uri _url = Uri.parse(ServerPath.apiBaseURL +
          '/Pasar/getPasarOverOfThisLatLong?' +
          _params);
      Response httpResponse = await get(_url);

      if (httpResponse.statusCode == 200) {
        Map dataObject = json.decode(httpResponse.body);
        List pasarLainAroundThisLatLong = dataObject['listPasar'];
        return pasarLainAroundThisLatLong;
      }
    }

    return [];
  }

  void initialization() async {
    List pasarLainLuarCA = await getPasarLainLuarCA();
    setState(() {
      isReadyToShow = true;
      pasarLainLuarCAState = pasarLainLuarCA;
      pasarLainLuarCAImmutableState = pasarLainLuarCA;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text('Pasar Lain di Luar Area Anda',
          style: TextStyle(
              fontSize: SizeConfig.horizontalBlock * 4,
              fontWeight: FontWeight.w700)),
      SizedBox(height: 15.0),
      (isReadyToShow)
          ? Column(children: [
              Pencarian(
                  hint: 'Cari Pasar dengan Nama',
                  onSearch: (String string) {
                    String stringLC = string.toLowerCase();

                    List pasarLainDalamCAFiltered =
                        pasarLainLuarCAImmutableState.where((pasar) {
                      String namaPasar = pasar['nama'];
                      String namaPasarLC = namaPasar.toLowerCase();

                      return namaPasarLC.indexOf(stringLC) >= 0;
                    }).toList();

                    setState(() {
                      pencarianString = string;
                      pasarLainLuarCAState = pasarLainDalamCAFiltered;
                    });
                  },
                  onClose: () {
                    setState(() {
                      pencarianString = '';
                      pasarLainLuarCAState = pasarLainLuarCAImmutableState;
                    });
                  },
                  text: pencarianString),
              SizedBox(height: 15.0),
              (pasarLainLuarCAState.length >= 1)
                  ? Container(
                      height: SizeConfig.horizontalBlock * 28,
                      child: ListView.builder(
                          scrollDirection: Axis.horizontal,
                          itemBuilder: (context, index) {
                            Map<String, dynamic> pasarLainDalamCAItem =
                                pasarLainLuarCAState[index];
                            return Row(children: [
                              PasarLainItem(pasarLainDalamCAItem),
                              SizedBox(width: 5)
                            ]);
                          },
                          itemCount: pasarLainLuarCAState.length),
                    )
                  : Padding(
                      padding: EdgeInsets.symmetric(vertical: 15.0),
                      child: Center(
                        child: Column(children: [
                          Image.asset(ClientPath.assetsImg + '/empty.png',
                              width: SizeConfig.horizontalBlock * 25),
                          SizedBox(height: 5),
                          Text('Yaahh.. belum ada pasar diluar area mu mak...',
                              style: TextStyle(
                                  color: CustomColors.textMuted,
                                  fontSize: SizeConfig.horizontalBlock * 3.5))
                        ]),
                      ),
                    )
            ])
          : Padding(
              padding: EdgeInsets.symmetric(vertical: 15.0),
              child: Center(
                  child: SizedBox(
                      width: 15,
                      height: 15,
                      child: CircularProgressIndicator(strokeWidth: 1.5))),
            )
    ]);
  }
}
