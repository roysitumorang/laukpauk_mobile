import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/models/PointModel.dart';
import 'package:laukpauk/models/UserModel.dart';

import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:laukpauk/models/AuthModel.dart';

class HistoriPoin extends StatefulWidget {
  _HistoriPoinState createState() => _HistoriPoinState();
}

class _HistoriPoinState extends State<HistoriPoin> {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return FutureBuilder<bool>(
        future: AuthModel.isLogin(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              bool isLogin = snapshot.data!;
              if (isLogin) {
                return Scaffold(
                    appBar: AppBar(
                      title: Text('Histori Poin'),
                      backgroundColor: CustomColors.green,
                    ),
                    body: RefreshIndicator(
                      onRefresh: () async {
                        setState(() {});
                      },
                      child: ListView(children: [
                        Container(
                          width: SizeConfig.horizontalBlock * 100,
                          child: Padding(
                            padding: EdgeInsets.symmetric(
                                horizontal: 5, vertical: 5),
                            child: Card(
                              elevation: 5,
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(10)),
                              child: Padding(
                                padding: EdgeInsets.symmetric(
                                    horizontal: 15, vertical: 10),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Padding(
                                      padding: EdgeInsets.only(
                                          top: 10.0,
                                          right: 10.0,
                                          left: 10.0,
                                          bottom: 5.0),
                                      child: Text('Poin Anda',
                                          style: TextStyle(
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      4.5,
                                              fontWeight: FontWeight.bold)),
                                    ),
                                    FutureBuilder<List>(
                                        future: UserModel.getHistoriPoint(),
                                        builder: (pointContext, snapshot) {
                                          if (snapshot.hasData) {
                                            if (snapshot.data != null) {
                                              List _historiPoint =
                                                  snapshot.data!;

                                              if (_historiPoint.length >= 1) {
                                                List<Widget>
                                                    _historiPointChildren =
                                                    _historiPoint
                                                        .map((histori) {
                                                  String _keterangan =
                                                      histori['keterangan'];

                                                  String _createdFrom =
                                                      histori['createdFrom'];

                                                  if (_createdFrom ==
                                                      PointModel
                                                          .createdFrom_transaksi) {
                                                    _keterangan = 'Transaksi ' +
                                                        _keterangan;
                                                  }

                                                  int _nilai = int.parse(
                                                      histori['nilai']);

                                                  return ListTile(
                                                      leading: Image.asset(
                                                        ClientPath.assetsImg +
                                                            '/poin.png',
                                                        width: 35.0,
                                                        height: 35.0,
                                                      ),
                                                      subtitle:
                                                          Text(_keterangan),
                                                      title: Text(
                                                        Currency.formatRupiah(
                                                            _nilai, 0),
                                                        style: TextStyle(
                                                            fontWeight:
                                                                FontWeight.w700,
                                                            color: (_nilai >= 0)
                                                                ? CustomColors
                                                                    .textSuccess
                                                                : CustomColors
                                                                    .textDanger),
                                                      ));
                                                }).toList();

                                                return SingleChildScrollView(
                                                  child: Column(
                                                      children:
                                                          _historiPointChildren),
                                                );
                                              } else {
                                                return Center(
                                                  child: Padding(
                                                    padding: EdgeInsets.symmetric(
                                                        vertical: SizeConfig
                                                                .horizontalBlock *
                                                            5.0),
                                                    child: Column(children: [
                                                      Image.asset(
                                                          ClientPath.assetsImg +
                                                              '/empty.png',
                                                          width: SizeConfig
                                                                  .horizontalBlock *
                                                              25.0),
                                                      Text(
                                                          'Tidak ada histori poin')
                                                    ]),
                                                  ),
                                                );
                                              }
                                            }
                                          }
                                          return Loading();
                                        })
                                  ],
                                ),
                              ),
                            ),
                          ),
                        )
                      ]),
                    ));
              }
            }
            return Login(routeTo: HistoriPoin());
          } else {
            return Scaffold(
                body: Loading(
              showLoadingImage: true,
              showCircularProgressIndicator: false,
              circularProgressIndicatorSize: Size(20.0, 20.0),
            ));
          }
        });
  }
}
