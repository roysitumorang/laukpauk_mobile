import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/penjual/components/PesananMasuk/PesananMasukItem.dart';

class PesananMasuk extends StatefulWidget {
  final int? statusTransaksi;
  PesananMasuk({this.statusTransaksi});
  _PesananMasukState createState() => _PesananMasukState();
}

class _PesananMasukState extends State<PesananMasuk>
    with TickerProviderStateMixin {
  int statusTransaksi = 0;
  int reload = 0;

  DateTime? selectedDateState;
  TextEditingController tanggalAwalController = TextEditingController();
  TextEditingController tanggalAkhirController = TextEditingController();

  Future<List> getPesananMasuk({String? statusTransaksi}) async {
    String statusTransaksiQS = '';
    if (statusTransaksi != null) {
      statusTransaksiQS = 'statusTransaksi=$statusTransaksi';
    }

    String _params = await HTTPRequest.getQueryString(
        authToken: true, params: statusTransaksiQS);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/getPesananMasuk?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List pesananMasuk = dataObject['pesananMasuk'];

      return pesananMasuk;
    }
    return [];
  }

  void initState() {
    super.initState();
    if (widget.statusTransaksi != null) {
      setState(() {
        statusTransaksi = widget.statusTransaksi!;
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

    String? statusTransaksiString, statusTransaksiStringReadble;
    if (statusTransaksi == 0) {
      statusTransaksiString = 'processing';
      statusTransaksiStringReadble = 'Orderan Baru';
    }
    if (statusTransaksi == 1) {
      statusTransaksiString = 'otw';
      statusTransaksiStringReadble = 'Sedang Dalam Perjalanan';
    }
    if (statusTransaksi == 2) {
      statusTransaksiString = 'received';
      statusTransaksiStringReadble = 'Sudah Diterima';
    }
    if (statusTransaksi == 3) {
      statusTransaksiString = 'rejected';
      statusTransaksiStringReadble = 'Ditolak';
    }

    DateTime selectedDate = DateTime.now();
    if (selectedDateState != null) {
      selectedDate = selectedDateState!;
    }

    bool isRentangWaktuNull = true;
    if (tanggalAwalController.text.length >= 1 &&
        tanggalAkhirController.text.length >= 1) {
      isRentangWaktuNull = false;
    }

    return Scaffold(
        appBar: AppBar(
          title: Text('Penjualan'),
          backgroundColor: CustomColors.green,
        ),
        body: Padding(
            padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Card(
                elevation: 5,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10)),
                child: Padding(
                  padding: EdgeInsets.symmetric(horizontal: 15, vertical: 10),
                  child: Column(
                    children: [
                      Padding(
                        padding: EdgeInsets.symmetric(horizontal: 10),
                        child: TabBar(
                            isScrollable: true,
                            onTap: (value) {
                              setState(() {
                                statusTransaksi = value;
                              });
                            },
                            unselectedLabelColor: CustomColors.textMuted,
                            indicatorColor: CustomColors.green,
                            labelColor: CustomColors.green,
                            controller: TabController(
                                length: 4,
                                vsync: this,
                                initialIndex: statusTransaksi),
                            tabs: [
                              Tab(
                                  child: Text('Orderan Baru',
                                      style: TextStyle(
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.25))),
                              Tab(
                                  child: Text('Dalam Perjalanan',
                                      style: TextStyle(
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.25))),
                              Tab(
                                  child: Text('Completed',
                                      style: TextStyle(
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.25))),
                              Tab(
                                  child: Text('Dibatalkan',
                                      style: TextStyle(
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.25)))
                            ]),
                      ),
                      Container(
                          margin: EdgeInsets.only(top: 10.0),
                          padding: EdgeInsets.symmetric(horizontal: 15.0),
                          decoration: BoxDecoration(
                              color: Color(0x5566CC99),
                              borderRadius: BorderRadius.circular(10.0)),
                          child: (statusTransaksi >= 2)
                              ? Row(
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceBetween,
                                  children: [
                                      Expanded(
                                        child: TextField(
                                            onTap: () async {
                                              DateTime? tanggalAwal =
                                                  await showDatePicker(
                                                      context: context,
                                                      initialDate: selectedDate,
                                                      firstDate: DateTime(2017),
                                                      lastDate: DateTime(2050));
                                              if (tanggalAwal != null) {
                                                String tahunString =
                                                    tanggalAwal.year.toString();
                                                String bulanString =
                                                    (tanggalAwal.month <= 9)
                                                        ? '0' +
                                                            tanggalAwal.month
                                                                .toString()
                                                        : tanggalAwal.month
                                                            .toString();
                                                String tanggalString =
                                                    tanggalAwal.day.toString();

                                                String formattedTanggalAwal =
                                                    tahunString +
                                                        '-' +
                                                        bulanString +
                                                        '-' +
                                                        tanggalString;
                                                tanggalAwalController.text =
                                                    formattedTanggalAwal;
                                              }
                                            },
                                            style: TextStyle(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3.25),
                                            controller: tanggalAwalController,
                                            decoration: InputDecoration(
                                                border: InputBorder.none,
                                                hintText: 'Tanggal Awal'),
                                            keyboardType: TextInputType.text),
                                      ),
                                      Expanded(
                                        child: TextField(
                                            onTap: () async {
                                              DateTime? tanggalAkhir =
                                                  await showDatePicker(
                                                      context: context,
                                                      initialDate: selectedDate,
                                                      firstDate: DateTime(2017),
                                                      lastDate: DateTime(2050));
                                              if (tanggalAkhir != null) {
                                                String tahunString =
                                                    tanggalAkhir.year
                                                        .toString();
                                                String bulanString =
                                                    (tanggalAkhir.month <= 9)
                                                        ? '0' +
                                                            tanggalAkhir.month
                                                                .toString()
                                                        : tanggalAkhir.month
                                                            .toString();
                                                String tanggalString =
                                                    tanggalAkhir.day.toString();

                                                String formattedTanggalAkhir =
                                                    tahunString +
                                                        '-' +
                                                        bulanString +
                                                        '-' +
                                                        tanggalString;
                                                tanggalAkhirController.text =
                                                    formattedTanggalAkhir;
                                              }
                                            },
                                            style: TextStyle(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3.25),
                                            controller: tanggalAkhirController,
                                            decoration: InputDecoration(
                                                border: InputBorder.none,
                                                hintText: 'Tanggal Akhir'),
                                            keyboardType: TextInputType.text),
                                      ),
                                      (!isRentangWaktuNull)
                                          ? InkWell(
                                              onTap: () {
                                                tanggalAwalController.text = '';
                                                tanggalAkhirController.text =
                                                    '';
                                                setState(() {});
                                              },
                                              child: Icon(Icons.close,
                                                  size: SizeConfig
                                                          .horizontalBlock *
                                                      5.5,
                                                  color:
                                                      CustomColors.textMuted),
                                            )
                                          : SizedBox(),
                                    ])
                              : SizedBox())
                    ],
                  ),
                ),
              ),
              FutureBuilder<List>(
                future: getPesananMasuk(statusTransaksi: statusTransaksiString),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    if (snapshot.data != null) {
                      List pesananMasuk = snapshot.data!;
                      if (pesananMasuk.length >= 1) {
                        List<Widget> pesananMasukWidget = pesananMasuk
                            .map((riwayat) => PesananMasukItem(riwayat))
                            .toList();
                        return Expanded(
                            child: RefreshIndicator(
                                onRefresh: () async {
                                  setState(() {});
                                },
                                child: ListView(children: pesananMasukWidget)));
                      } else {
                        return Expanded(
                          child: RefreshIndicator(
                            onRefresh: () async {
                              setState(() {});
                            },
                            child:
                                LayoutBuilder(builder: (context, constraints) {
                              return ListView(
                                shrinkWrap: true,
                                children: [
                                  Container(
                                    constraints: BoxConstraints(
                                        minHeight: constraints.maxHeight),
                                    child: Padding(
                                      padding: EdgeInsets.symmetric(
                                          vertical: 15, horizontal: 15.0),
                                      child: Column(
                                          mainAxisAlignment:
                                              MainAxisAlignment.center,
                                          children: [
                                            Image.asset(
                                                ClientPath.assetsImg +
                                                    '/empty.png',
                                                width:
                                                    SizeConfig.horizontalBlock *
                                                        30),
                                            SizedBox(height: 10),
                                            Text(
                                                'Transaksi dengan status "$statusTransaksiStringReadble" tidak Ditemukan',
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
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
                    }
                  }
                  return Center(
                      child: SizedBox(
                          width: 15,
                          height: 15,
                          child: CircularProgressIndicator(strokeWidth: 1.5)));
                },
              )
            ])));
  }
}
