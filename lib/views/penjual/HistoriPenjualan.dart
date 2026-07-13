import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/models/HistoriPenjualanModel.dart';
import 'package:laukpauk/views/penjual/components/HistoriPenjualan/HistoriPenjualanItem.dart';

class HistoriPenjualan extends StatefulWidget {
  final String? idPenjual;
  HistoriPenjualan({this.idPenjual});
  _HistoriPenjualanState createState() => _HistoriPenjualanState();
}

class _HistoriPenjualanState extends State<HistoriPenjualan>
    with TickerProviderStateMixin {
  int? statusTransaksi;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String? statusTransaksiString;
    if (statusTransaksi == 2) {
      statusTransaksiString = 'received';
    }

    return Scaffold(
        appBar: AppBar(
            title: Text('Histori Penjualan Saya'),
            backgroundColor: CustomColors.green),
        backgroundColor: CustomColors.aliceBlue,
        body: Padding(
          padding: EdgeInsets.symmetric(horizontal: 10, vertical: 10),
          child: Column(children: [
            Card(
              elevation: 5,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              child: Container(
                width: SizeConfig.horizontalBlock * 100,
                padding: EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                child: Center(
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
                          length: 3,
                          vsync: this,
                          initialIndex: statusTransaksi ?? 0),
                      tabs: [
                        Tab(
                            child: Text('Hari Ini',
                                style: TextStyle(
                                    fontSize:
                                        SizeConfig.horizontalBlock * 3.25))),
                        Tab(
                            child: Text('Semua',
                                style: TextStyle(
                                    fontSize:
                                        SizeConfig.horizontalBlock * 3.25))),
                        Tab(
                            child: Text('Completed',
                                style: TextStyle(
                                    fontSize:
                                        SizeConfig.horizontalBlock * 3.25)))
                      ]),
                ),
              ),
            ),
            SizedBox(height: 3.5),
            FutureBuilder<List>(
                future: HistoriPenjualanModel.getHistoriPenjualan(
                    idPenjual: widget.idPenjual,
                    select:
                        'namaUser, waktuCheckout, nomorTransaksi, status, id',
                    statusTransaksi: statusTransaksiString),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    if (snapshot.data != null) {
                      List historiPenjualan = snapshot.data!;

                      if (historiPenjualan.length >= 1) {
                        List<Widget> historiPenjualanItemWidget =
                            historiPenjualan
                                .map((penjualan) =>
                                    HistoriPenjualanItem(penjualan))
                                .toList();

                        return Expanded(
                            child: RefreshIndicator(
                          onRefresh: () async {
                            setState(() {});
                          },
                          child: ListView(
                              padding: EdgeInsets.zero,
                              children: historiPenjualanItemWidget),
                        ));
                      } else {
                        return Expanded(
                          child: LayoutBuilder(builder: (context, constraints) {
                            return RefreshIndicator(
                              onRefresh: () async {
                                setState(() {});
                              },
                              child: ListView(
                                children: [
                                  Container(
                                    constraints: BoxConstraints(
                                        minHeight: constraints.maxHeight),
                                    child: Column(
                                        mainAxisAlignment:
                                            MainAxisAlignment.center,
                                        children: [
                                          Image.asset(
                                              ClientPath.assetsImg +
                                                  '/empty.png',
                                              width:
                                                  SizeConfig.horizontalBlock *
                                                      35),
                                          SizedBox(height: 10),
                                          Text(
                                              'Histori transaksi tidak ditemukan',
                                              style: TextStyle(
                                                  color: CustomColors.textMuted,
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.5))
                                        ]),
                                  ),
                                ],
                              ),
                            );
                          }),
                        );
                      }
                    }
                  }
                  return Expanded(
                    child: Center(
                        child: SizedBox(
                            width: 25,
                            height: 25,
                            child:
                                CircularProgressIndicator(strokeWidth: 1.5))),
                  );
                })
          ]),
        ));
  }
}
