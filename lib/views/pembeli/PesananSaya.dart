import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:laukpauk/models/AuthModel.dart';

import 'package:laukpauk/views/pembeli/components/PesananSaya/PesananSayaItem.dart';

class PesananSaya extends StatefulWidget {
  final int? statusTransaksi;
  PesananSaya({this.statusTransaksi});
  _PesananSayaState createState() => _PesananSayaState();
}

class _PesananSayaState extends State<PesananSaya>
    with TickerProviderStateMixin {
  int _statusTransaksiState = 0;
  int reload = 0;
  String statusTransaksiHariIni = '';

  TextEditingController tanggalAwalController = TextEditingController();
  TextEditingController tanggalAkhirController = TextEditingController();

  DateTime? selectedDateState;

  Future<List> _getRiwayatTransaksi({String? statusTransaksi}) async {
    String statusTransaksiQS =
        (statusTransaksi == null) ? '' : 'statusTransaksi=$statusTransaksi';
    String statusTransaksiHariIniQS =
        (statusTransaksi == null && statusTransaksiHariIni != '')
            ? '&statusTransaksiHariIni=$statusTransaksiHariIni'
            : '';
            
    String rentangWaktuQS = '';
    if (statusTransaksi != null) {
      String rentangWaktuAwal, rentangWaktuAkhir;

      String tglAwal = tanggalAwalController.text;
      String tglAkhir = tanggalAkhirController.text;

      rentangWaktuAwal = (tglAwal.length >= 1) ? '&rentangAwal=$tglAwal' : '';
      rentangWaktuAkhir =
          (tglAkhir.length >= 1) ? '&rentangAkhir=$tglAkhir' : '';
      rentangWaktuQS = rentangWaktuAwal + rentangWaktuAkhir;
    }

    String _params = await HTTPRequest.getQueryString(
        authToken: true,
        params:
            '$statusTransaksiQS$statusTransaksiHariIniQS$rentangWaktuQS');

    String _urlString = ServerPath.apiBaseURL + '/User/getRiwayatTransaksi?' + _params;
    Uri _url = Uri.parse(_urlString);

    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List riwayatTransaksi = dataObject['riwayatTransaksi'];

      return riwayatTransaksi;
    }

    return [];
  }

  void initState() {
    super.initState();
    if (widget.statusTransaksi != null) {
      setState(() {
        _statusTransaksiState = widget.statusTransaksi!;
      });
    }
  }

  void dispose() {
    super.dispose();
    tanggalAwalController.dispose();
    tanggalAkhirController.dispose();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String? statusTransaksiString;
    if (_statusTransaksiState == 1) {
      statusTransaksiString = 'pending';
    }
    if (_statusTransaksiState == 2) {
      statusTransaksiString = 'processing';
    }
    if (_statusTransaksiState == 3) {
      statusTransaksiString = 'otw';
    }
    if (_statusTransaksiState == 4) {
      statusTransaksiString = 'received';
    }
    if (_statusTransaksiState == 5) {
      statusTransaksiString = 'rejected';
    }

    DateTime selectedDate = DateTime.now();
    if (selectedDateState != null) {
      selectedDate = selectedDateState!;
    }

    List<DropdownMenuItem<String>> dropdownButtonStatusTransaksi = [
      DropdownMenuItem(
          child: Text('Semua Status Transaksi',
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
          value: ''),
      DropdownMenuItem(
          child: Text('Belum di Proses',
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
          value: 'pending'),
      DropdownMenuItem(
          child: Text('Sedang di proses',
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
          value: 'processing'),
      DropdownMenuItem(
          child: Text('Sedang dalam perjalanan',
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
          value: 'otw'),
      DropdownMenuItem(
          child: Text('Sudah diterima',
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
          value: 'received'),
      DropdownMenuItem(
          child: Text('Dibatalkan',
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
          value: 'rejected')
    ];

    bool isRentangWaktuNull = true;
    if (tanggalAwalController.text.length >= 1 &&
        tanggalAkhirController.text.length >= 1) {
      isRentangWaktuNull = false;
    }

    return FutureBuilder<bool>(
        future: AuthModel.isLogin(),
        builder: (BuildContext authContext, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              bool _isLogin = snapshot.data!;

              if (_isLogin) {
                return Scaffold(
                    appBar: AppBar(
                      title: Text('Riwayat Transaksi'),
                      backgroundColor: CustomColors.green,
                    ),
                    body: Padding(
                        padding:
                            EdgeInsets.symmetric(horizontal: 5, vertical: 5),
                        child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Card(
                                elevation: 5,
                                shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(10)),
                                child: Padding(
                                  padding: EdgeInsets.symmetric(
                                      horizontal: 15, vertical: 10),
                                  child: Column(
                                    children: [
                                      Padding(
                                        padding: EdgeInsets.symmetric(
                                            horizontal: 10),
                                        child: TabBar(
                                            isScrollable: true,
                                            onTap: (value) {
                                              setState(() {
                                                _statusTransaksiState = value;
                                              });
                                            },
                                            unselectedLabelColor:
                                                CustomColors.textMuted,
                                            indicatorColor: CustomColors.green,
                                            labelColor: CustomColors.green,
                                            controller: TabController(
                                                length: 6,
                                                vsync: this,
                                                initialIndex:
                                                    _statusTransaksiState),
                                            tabs: [
                                              Tab(
                                                  child: Text('Hari Ini',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25))),
                                              Tab(
                                                  child: Text('Belum di Proses',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25))),
                                              Tab(
                                                  child: Text('Dalam Proses',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25))),
                                              Tab(
                                                  child: Text(
                                                      'Dalam Perjalanan',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25))),
                                              Tab(
                                                  child: Text('Completed',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25))),
                                              Tab(
                                                  child: Text('Dibatalkan',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25)))
                                            ]),
                                      ),
                                      Container(
                                        margin: EdgeInsets.only(top: 10.0),
                                        padding: EdgeInsets.symmetric(
                                            horizontal: 15.0),
                                        decoration: BoxDecoration(
                                            color: Color(0x5566CC99),
                                            borderRadius:
                                                BorderRadius.circular(10.0)),
                                        child: (_statusTransaksiState == 0)
                                            ? DropdownButton<String>(
                                                underline:
                                                    DropdownButtonHideUnderline(
                                                        child: SizedBox()),
                                                isExpanded: true,
                                                items:
                                                    dropdownButtonStatusTransaksi,
                                                value: statusTransaksiHariIni,
                                                onChanged: (newValue) {
                                                  if (newValue != null) {
                                                    setState(() {
                                                      statusTransaksiHariIni =
                                                          newValue;
                                                    });
                                                  }
                                                })
                                            : Row(
                                                mainAxisAlignment:
                                                    MainAxisAlignment
                                                        .spaceBetween,
                                                children: [
                                                    Expanded(
                                                      child: TextField(
                                                          onTap: () async {
                                                            DateTime?
                                                                tanggalAwal =
                                                                await showDatePicker(
                                                                    context:
                                                                        context,
                                                                    initialDate:
                                                                        selectedDate,
                                                                    firstDate:
                                                                        DateTime(
                                                                            2017),
                                                                    lastDate:
                                                                        DateTime(
                                                                            2050));
                                                            if (tanggalAwal !=
                                                                null) {
                                                              String
                                                                  tahunString =
                                                                  tanggalAwal
                                                                      .year
                                                                      .toString();
                                                              String bulanString = (tanggalAwal
                                                                          .month <=
                                                                      9)
                                                                  ? '0' +
                                                                      tanggalAwal
                                                                          .month
                                                                          .toString()
                                                                  : tanggalAwal
                                                                      .month
                                                                      .toString();
                                                              String
                                                                  tanggalString =
                                                                  tanggalAwal
                                                                      .day
                                                                      .toString();

                                                              String
                                                                  formattedTanggalAwal =
                                                                  tahunString +
                                                                      '-' +
                                                                      bulanString +
                                                                      '-' +
                                                                      tanggalString;
                                                              tanggalAwalController
                                                                      .text =
                                                                  formattedTanggalAwal;
                                                            }
                                                          },
                                                          style: TextStyle(
                                                              fontSize: SizeConfig
                                                                      .horizontalBlock *
                                                                  3.25),
                                                          controller:
                                                              tanggalAwalController,
                                                          decoration:
                                                              InputDecoration(
                                                                  border:
                                                                      InputBorder
                                                                          .none,
                                                                  hintText:
                                                                      'Tanggal Awal'),
                                                          keyboardType:
                                                              TextInputType
                                                                  .text),
                                                    ),
                                                    Expanded(
                                                      child: TextField(
                                                          onTap: () async {
                                                            DateTime?
                                                                tanggalAkhir =
                                                                await showDatePicker(
                                                                    context:
                                                                        context,
                                                                    initialDate:
                                                                        selectedDate,
                                                                    firstDate:
                                                                        DateTime(
                                                                            2017),
                                                                    lastDate:
                                                                        DateTime(
                                                                            2050));
                                                            if (tanggalAkhir !=
                                                                null) {
                                                              String
                                                                  tahunString =
                                                                  tanggalAkhir
                                                                      .year
                                                                      .toString();
                                                              String bulanString = (tanggalAkhir
                                                                          .month <=
                                                                      9)
                                                                  ? '0' +
                                                                      tanggalAkhir
                                                                          .month
                                                                          .toString()
                                                                  : tanggalAkhir
                                                                      .month
                                                                      .toString();
                                                              String
                                                                  tanggalString =
                                                                  tanggalAkhir
                                                                      .day
                                                                      .toString();

                                                              String
                                                                  formattedTanggalAkhir =
                                                                  tahunString +
                                                                      '-' +
                                                                      bulanString +
                                                                      '-' +
                                                                      tanggalString;
                                                              tanggalAkhirController
                                                                      .text =
                                                                  formattedTanggalAkhir;
                                                            }
                                                          },
                                                          style: TextStyle(
                                                              fontSize: SizeConfig
                                                                      .horizontalBlock *
                                                                  3.25),
                                                          controller:
                                                              tanggalAkhirController,
                                                          decoration:
                                                              InputDecoration(
                                                                  border:
                                                                      InputBorder
                                                                          .none,
                                                                  hintText:
                                                                      'Tanggal Akhir'),
                                                          keyboardType:
                                                              TextInputType
                                                                  .text),
                                                    ),
                                                    (!isRentangWaktuNull)
                                                        ? InkWell(
                                                            onTap: () {
                                                              tanggalAwalController
                                                                  .text = '';
                                                              tanggalAkhirController
                                                                  .text = '';
                                                              setState(() {});
                                                            },
                                                            child: Icon(
                                                                Icons.close,
                                                                size: SizeConfig
                                                                        .horizontalBlock *
                                                                    5.5,
                                                                color: CustomColors
                                                                    .textMuted),
                                                          )
                                                        : SizedBox(),
                                                  ]),
                                      )
                                    ],
                                  ),
                                ),
                              ),
                              FutureBuilder<List>(
                                  future: _getRiwayatTransaksi(
                                      statusTransaksi: statusTransaksiString),
                                  builder: (context, snapshot) {
                                    if (snapshot.hasData) {
                                      List? riwayatTransaksi = snapshot.data;
                                      if (riwayatTransaksi != null) {
                                        if (riwayatTransaksi.length >= 1) {
                                          List<Widget> riwayatTransaksiWidget =
                                              riwayatTransaksi
                                                  .map((riwayat) =>
                                                      PesananSayaItem(riwayat,
                                                          pesananDiterimaCallback:
                                                              (statusPesananDiterima) {
                                                        String message =
                                                            'Gagal menandai pesanan sebagai SUDAH DITERIMA. Silahkan coba lagi!';
                                                        if (statusPesananDiterima) {
                                                          setState(() {
                                                            reload++;
                                                          });
                                                          message =
                                                              'Berhasil menanda pesanan sebagai SUDAH DITERIMA !';
                                                        }
                                                        SnackBar snackBar =
                                                            SnackBar(
                                                                duration:
                                                                    Duration(
                                                                        seconds:
                                                                            3),
                                                                content: Text(
                                                                    message));

                                                        // Scaffold.of(context).showSnackBar(snackBar);
                                                        ScaffoldMessenger.of(
                                                                context)
                                                            .showSnackBar(
                                                                snackBar);
                                                      }))
                                                  .toList();
                                          return Expanded(
                                              child: RefreshIndicator(
                                                  onRefresh: () async {
                                                    setState(() {});
                                                  },
                                                  child: ListView(
                                                      children:
                                                          riwayatTransaksiWidget)));
                                        } else {
                                          return Expanded(
                                            child: RefreshIndicator(
                                              onRefresh: () async {
                                                setState(() {});
                                              },
                                              child: LayoutBuilder(builder:
                                                  (context, constraints) {
                                                return ListView(
                                                  shrinkWrap: true,
                                                  children: [
                                                    Container(
                                                      constraints: BoxConstraints(
                                                          minHeight: constraints
                                                              .maxHeight),
                                                      child: Padding(
                                                        padding: EdgeInsets
                                                            .symmetric(
                                                                vertical: 15),
                                                        child: Column(
                                                            mainAxisAlignment:
                                                                MainAxisAlignment
                                                                    .center,
                                                            children: [
                                                              Image.asset(
                                                                  ClientPath
                                                                          .assetsImg +
                                                                      '/empty.png',
                                                                  width: SizeConfig
                                                                          .horizontalBlock *
                                                                      30),
                                                              SizedBox(
                                                                  height: 10),
                                                              Text(
                                                                  'Riwayat Transaksi tidak Ditemukan',
                                                                  style: TextStyle(
                                                                      fontSize:
                                                                          SizeConfig.horizontalBlock *
                                                                              3))
                                                            ]),
                                                      ),
                                                    ),
                                                  ],
                                                );
                                              }),
                                            ),
                                          );
                                        }
                                      } else {
                                        return Expanded(child: Loading());
                                      }
                                    } else {
                                      return Expanded(
                                          child: Center(child: Loading()));
                                    }
                                  })
                            ])),
                    bottomNavigationBar: Footer());
              }
            }
            return Scaffold(
                body: Login(routeTo: PesananSaya()),
                bottomNavigationBar: Footer());
          } else {
            return Scaffold(body: Loading());
          }
        });
  }
}
