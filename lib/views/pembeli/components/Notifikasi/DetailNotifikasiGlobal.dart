// import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/models/NotifikasiModel.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class DetailNotifikasiGlobal extends StatefulWidget {
  final String idNotifikasi;
  DetailNotifikasiGlobal(this.idNotifikasi);

  _DetailNotifikasiGlobalState createState() => _DetailNotifikasiGlobalState();
}

class _DetailNotifikasiGlobalState extends State<DetailNotifikasiGlobal> {
  dynamic detailNotifikasiGlobalState;

  void initialization() async {
    String idNotifikasi = widget.idNotifikasi;
    dynamic detailNotifikasiGlobal =
        await NotifikasiModel.getDetailNotifikasiGlobal(idNotifikasi);
    setState(() {
      detailNotifikasiGlobalState = detailNotifikasiGlobal;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);
    dynamic _dNGS = detailNotifikasiGlobalState;

    bool _isDNGSNull = detailNotifikasiGlobalState == null;

    bool isFotoOkay = false;
    if (!_isDNGSNull) {
      if (_dNGS != false) {
        if (_dNGS['foto'] != null && _dNGS['foto'].length >= 1) {
          isFotoOkay = true;
        }
      }
    }

    ImageProvider<Object> _image;
    if (isFotoOkay) {
      _image = NetworkImage(ServerPath.baseURL +
          '/' +
          ServerPath.uploadGambarPushNotifikasi +
          '/' +
          _dNGS['foto']);
    } else {
      _image = AssetImage(ClientPath.assetsImg + '/laukpauk-icon.png');
    }

    // Map<String, dynamic> _response = {
    //   'idNotifikasi': widget.idNotifikasi,
    //   'dNGS': _dNGS
    // };
    // return Scaffold(body: Center(child: Text(jsonEncode(_response))));

    return Scaffold(
      appBar: (!_isDNGSNull)
          ? AppBar(
              elevation: 0.0,
              title: (_dNGS != false) ? Text(_dNGS['judul']) : null,
              backgroundColor: CustomColors.green)
          : null,
      backgroundColor: CustomColors.nearlyWhite,
      body: (!_isDNGSNull)
          ? (_dNGS != false)
              ? Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                  Container(
                    padding: (isFotoOkay)
                        ? null
                        : EdgeInsets.symmetric(vertical: 5.0),
                    child: Container(
                      width: SizeConfig.horizontalBlock * 100,
                      height: SizeConfig.horizontalBlock * 60,
                      decoration: BoxDecoration(
                          image: DecorationImage(
                              scale: 2.0,
                              fit: (isFotoOkay) ? BoxFit.cover : null,
                              image: _image)),
                    ),
                  ),
                  SizedBox(height: 15.0),
                  Padding(
                    padding: EdgeInsets.symmetric(horizontal: 15.0),
                    child: Text(_dNGS['judul'],
                        style: TextStyle(
                            color: CustomColors.green,
                            fontSize: SizeConfig.horizontalBlock * 4.0,
                            fontWeight: FontWeight.w500)),
                  ),
                  SizedBox(height: 15.0),
                  Padding(
                    padding: EdgeInsets.symmetric(horizontal: 15.0),
                    child: Text(_dNGS['pesan'],
                        style: TextStyle(
                            color: CustomColors.textMuted,
                            fontSize: SizeConfig.horizontalBlock * 3.25)),
                  ),
                  SizedBox(height: 15.0),
                  Padding(
                    padding: EdgeInsets.symmetric(horizontal: 15.0),
                    child: Text('Pada ' + _dNGS['createdAt'],
                        style: TextStyle(
                            color: CustomColors.textMuted,
                            fontSize: SizeConfig.horizontalBlock * 3.0)),
                  ),
                ])
              : Center(child: Text('Notifikasi Tidak Terdaftar !'))
          : Loading(),
      bottomNavigationBar: Footer(),
    );
  }
}
