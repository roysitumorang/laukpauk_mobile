import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:laukpauk/views/pembeli/components/HistoriDompet/HistoriDompetItem.dart';
import 'package:laukpauk/models/AuthModel.dart';

class HistoriDompet extends StatefulWidget {
  final String? jenisDompet;
  HistoriDompet({this.jenisDompet});
  _HistoriDompetState createState() => _HistoriDompetState();
}

class _HistoriDompetState extends State<HistoriDompet>
    with TickerProviderStateMixin {
  int jenisDompet = 0;
  //0 = semua jenis dompet
  //1 = jenis dompet referral
  //2 = jenis dompet potongantransaksi

  void initialization() async {
    String? jenisDompetFromWidget = widget.jenisDompet;

    if (jenisDompetFromWidget != null) {
      int jenisDompetInt = 0;
      if (jenisDompetFromWidget.toLowerCase() == 'referral') {
        jenisDompetInt = 1;
      }
      if (jenisDompetFromWidget.toLowerCase() == 'potongantransaksi') {
        jenisDompetInt = 2;
      }

      setState(() {
        jenisDompet = jenisDompetInt;
      });
    }
  }

  void initState() {
    super.initState();
    initialization();
  }

  Future<List> getRiwayatDompet(int jenisDompet) async {
    String jenisDompetString = '';

    if (jenisDompet == 1) {
      jenisDompetString = 'jenisDompet=referral';
    }

    if (jenisDompet == 2) {
      jenisDompetString = 'jenisDompet=potongantransaksi';
    }

    String _params = await HTTPRequest.getQueryString(
        authToken: true, params: jenisDompetString);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getRiwayatDompet?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = json.decode(httpResponse.body);
      List riwayatDompet = dataObject['riwayatDompet'];

      return riwayatDompet;
    }
    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return FutureBuilder<bool?>(
        future: AuthModel.isLogin(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              bool isLogin = snapshot.data!;
              if (isLogin) {
                return Scaffold(
                    appBar: AppBar(
                      title: Text('Histori Dompet'),
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
                                  children: [
                                    Padding(
                                      padding:
                                          EdgeInsets.symmetric(horizontal: 10),
                                      child: TabBar(
                                          isScrollable: true,
                                          onTap: (value) {
                                            setState(() {
                                              jenisDompet = value;
                                            });
                                          },
                                          unselectedLabelColor:
                                              CustomColors.textMuted,
                                          indicatorColor: CustomColors.green,
                                          labelColor: CustomColors.green,
                                          controller: TabController(
                                              length: 3,
                                              vsync: this,
                                              initialIndex: jenisDompet),
                                          tabs: [
                                            Tab(
                                                child: Text('Semua',
                                                    style: TextStyle(
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            3.5))),
                                            Tab(
                                                child: Text('Referral',
                                                    style: TextStyle(
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            3.5))),
                                            Tab(
                                                child: Text(
                                                    'Potongan Transaksi',
                                                    style: TextStyle(
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            3.5)))
                                          ]),
                                    ),
                                    SizedBox(height: 25),
                                    FutureBuilder<List>(
                                        future: getRiwayatDompet(jenisDompet),
                                        builder: (context, snapshot) {
                                          if (snapshot.hasData) {
                                            if (snapshot.data != null) {
                                              List riwayatDompet =
                                                  snapshot.data!;
                                              if (riwayatDompet.length >= 1) {
                                                int nomor = 1;
                                                List<Widget>
                                                    riwayatDompetWidget =
                                                    riwayatDompet
                                                        .map((riwayat) {
                                                  int indexData = nomor;
                                                  nomor++;
                                                  return HistoriDompetItem(
                                                      indexData, riwayat);
                                                }).toList();

                                                return Column(
                                                    children:
                                                        riwayatDompetWidget);
                                              } else {
                                                return Padding(
                                                  padding: EdgeInsets.only(
                                                      bottom: 15),
                                                  child: Column(children: [
                                                    Image.asset(
                                                        ClientPath.assetsImg +
                                                            '/empty.png',
                                                        width: SizeConfig
                                                                .horizontalBlock *
                                                            30),
                                                    SizedBox(height: 10),
                                                    Text(
                                                        'Tidak ada riwayat dompet',
                                                        style: TextStyle(
                                                            color: CustomColors
                                                                .textMuted,
                                                            fontSize: SizeConfig
                                                                    .horizontalBlock *
                                                                3.5))
                                                  ]),
                                                );
                                              }
                                            }
                                          }
                                          return Padding(
                                            padding:
                                                EdgeInsets.only(bottom: 25.0),
                                            child: Loading(
                                                showLoadingImage: true,
                                                showCircularProgressIndicator:
                                                    false,
                                                circularProgressIndicatorSize:
                                                    Size(20.0, 20.0)),
                                          );
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
            return Login(routeTo: HistoriDompet());
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
