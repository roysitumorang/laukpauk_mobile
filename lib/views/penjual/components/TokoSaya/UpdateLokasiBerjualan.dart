import 'package:flutter/material.dart';
import 'package:laukpauk/models/TokoSayaModel.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/models/ADJDModel.dart';

import 'package:laukpauk/views/pembeli/PinGMAPS.dart';
import 'package:laukpauk/views/pembeli/components/OTPVerifikasiUser/ListPasar.dart';

class UpdateLokasiBerjualan extends StatefulWidget {
  final Map<String, dynamic> dataPenjual;
  final Function(bool statusUbahLokasiBerjualan)
      statusUbahLokasiBerjualanCallback;
  UpdateLokasiBerjualan(
      this.dataPenjual, this.statusUbahLokasiBerjualanCallback);
  _UpdateLokasiBerjualanState createState() => _UpdateLokasiBerjualanState();
}

class _UpdateLokasiBerjualanState extends State<UpdateLokasiBerjualan> {
  dynamic
      lokasiBaruBerjualanSelected; //map kalau pakai gmaps, string kalau pakai pasar yang sudah ada
  String lokasiBerjualanTerpilih = 'pasarTersedia';

  TextStyle judul({Color? color}) {
    return TextStyle(
        color: (color == null) ? CustomColors.nearlyBlack : color,
        fontSize: SizeConfig.horizontalBlock * 3.5,
        fontWeight: FontWeight.w700);
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> dataPenjual = widget.dataPenjual;

    return Container(
        padding: EdgeInsets.symmetric(vertical: 20, horizontal: 15),
        child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
                InkWell(
                    onTap: () {
                      setState(() {
                        lokasiBerjualanTerpilih = 'pasarTersedia';
                        lokasiBaruBerjualanSelected = null;
                      });
                    },
                    child: Text('Pasar Tersedia',
                        style: judul(color: CustomColors.textInfo))),
                InkWell(
                  onTap: () async {
                    Map<String, dynamic>? pinnedLocationMap =
                        await Navigator.push(context,
                            MaterialPageRoute(builder: (_) => PinGMAPS()));
                    if (pinnedLocationMap != null) {
                      String latitude = pinnedLocationMap['lat'];
                      String longitude = pinnedLocationMap['lng'];

                      String pinnedLocationString =
                          await ADJDModel.reverseGeocoding(
                              latitude: latitude, longitude: longitude);

                      Map<String, dynamic> lokasiBaru = {
                        'latitude': latitude,
                        'longitude': longitude,
                        'readAbleAddress': pinnedLocationString
                      };

                      setState(() {
                        lokasiBaruBerjualanSelected = lokasiBaru;
                        lokasiBerjualanTerpilih = 'lokasiTersendiri';
                      });
                    }
                  },
                  child: Text('Lokasi Tersendiri',
                      style: judul(color: CustomColors.textPrimary)),
                )
              ]),
              SizedBox(height: 10),
              (lokasiBerjualanTerpilih == 'pasarTersedia')
                  ? ListPasar(
                      pasarSelected: lokasiBaruBerjualanSelected,
                      onPasarClicked: (idPasar) {
                        setState(() {
                          lokasiBaruBerjualanSelected = idPasar;
                          lokasiBerjualanTerpilih = 'pasarTersedia';
                        });
                      })
                  : Text(
                      'Lokasi baru anda berjualan di sekitar jalan ' +
                          lokasiBaruBerjualanSelected['readAbleAddress'],
                      style: TextStyle(
                          fontSize: SizeConfig.horizontalBlock * 3.25)),
              (lokasiBaruBerjualanSelected != null)
                  ? Container(
                      padding: EdgeInsets.only(top: 5),
                      child: Center(
                          child: TextButton(
                              onPressed: () async {
                                String idPenjual = dataPenjual['id'].toString();
                                Map<String, dynamic> lokasiBaru = {
                                  'jenisPasarBaru': lokasiBerjualanTerpilih
                                };
                                if (lokasiBerjualanTerpilih ==
                                    'lokasiTersendiri') {
                                  lokasiBaru['latPasarBaru'] =
                                      lokasiBaruBerjualanSelected['latitude'];
                                  lokasiBaru['longPasarBaru'] =
                                      lokasiBaruBerjualanSelected['longitude'];
                                } else {
                                  lokasiBaru['idPasarBaru'] =
                                      lokasiBaruBerjualanSelected;
                                }

                                bool result =
                                    await TokoSayaModel.ubahLokasiBerjualan(
                                        idPenjual, lokasiBaru);

                                widget
                                    .statusUbahLokasiBerjualanCallback(result);
                              },
                              child: Text('Tetapkan Lokasi Baru Berjualan',
                                  style: judul(color: CustomColors.green)))),
                    )
                  : SizedBox()
            ]));
  }
}
