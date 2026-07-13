import 'package:flutter/material.dart';
import 'package:laukpauk/views/pembeli/components/ListProduk/ListPenjualItem.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/views/pembeli/components/ListProduk/ListProdukItem.dart';

import 'package:laukpauk/global-components/Pencarian.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/models/ADJDModel.dart';

import 'package:laukpauk/models/ProdukModel.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

class ListProduk extends StatefulWidget {
  final String title, subTitle;
  final List dataProduk;
  final List? dataPenjual;
  final bool isReadyToShow;
  final void Function()? onRefresh;

  ListProduk(
      {required this.title,
      required this.subTitle,
      required this.dataProduk,
      required this.isReadyToShow,
      required this.onRefresh,
      this.dataPenjual});
  _ListProdukState createState() => _ListProdukState();
}

class _ListProdukState extends State<ListProduk> {
  List dataProdukState = [];
  List dataProdukImmutableState = [];
  List dataPenjualState = [];
  List dataPenjualImmutableState = [];
  String pencarian = '';

  String shownView = 'produk'; //produk || penjual

  void initialization() async {
    print(widget.dataProduk);
    List dataProduk = widget.dataProduk;
    List? dataPenjual = widget.dataPenjual;

    if (dataPenjual == null) {
      dataPenjual = [];
    }

    setState(() {
      dataProdukState = dataProduk;
      dataProdukImmutableState = dataProduk;
      dataPenjualState = dataPenjual ?? [];
      dataPenjualImmutableState = dataPenjual ?? [];
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  final EdgeInsets _horizontalPadding = EdgeInsets.symmetric(horizontal: 15.0);

  Widget build(BuildContext context) {
    bool isReadyToShow = widget.isReadyToShow;
    String title = widget.title;
    // String subTitle = widget.subTitle;

    List<Widget> listDataProdukWidget = [];
    List<Widget> listDataPenjualWidget = [];

    if (isReadyToShow) {
      if (shownView == 'produk') {
        if (dataProdukState.length >= 1) {
          listDataProdukWidget = dataProdukState
              .map((produk) => ListProdukItem(produk, () async {
                    ADJDModel.deleteAlamatDanJadwalDelivery();
                    Map<String, dynamic> adjdSet =
                        await HeaderModel.adjd(context);

                    String? latitude = adjdSet['latitude'];
                    String? longitude = adjdSet['longitude'];
                    String? kodeJadwalDelivery = adjdSet['kodeJadwalDelivery'];

                    bool isADJDCompleted = false;
                    if (latitude != null &&
                        longitude != null &&
                        kodeJadwalDelivery != null) {
                      isADJDCompleted = true;
                      Provider.of<HeaderViewModel>(context, listen: false)
                          .isADJDSet = isADJDCompleted;
                      await ProdukModel.showSatuanProduk(context, produk);
                    }
                  }))
              .toList();
        }
      } else {
        if (dataPenjualState.length >= 1) {
          listDataPenjualWidget = dataPenjualState
              .map((penjual) => ListPenjualItem(penjual, () async {
                    ADJDModel.deleteAlamatDanJadwalDelivery();
                    Map<String, dynamic> adjdSet =
                        await HeaderModel.adjd(context);

                    String? latitude = adjdSet['latitude'];
                    String? longitude = adjdSet['longitude'];
                    String? kodeJadwalDelivery = adjdSet['kodeJadwalDelivery'];

                    bool isADJDCompleted = false;
                    if (latitude != null &&
                        longitude != null &&
                        kodeJadwalDelivery != null) {
                      isADJDCompleted = true;
                      Provider.of<HeaderViewModel>(context, listen: false)
                          .isADJDSet = isADJDCompleted;
                    }
                  }))
              .toList();
        }
      }
    }

    List<Widget> dataToShow =
        (shownView == 'produk') ? listDataProdukWidget : listDataPenjualWidget;

    return Scaffold(
        body: (isReadyToShow)
            ? Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Header(),
                  Padding(
                      padding: EdgeInsets.only(
                          left: 15.0, right: 15.0, top: 15.0, bottom: 2.5),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Expanded(
                            child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(title,
                                      style: TextStyle(
                                          fontSize:
                                              SizeConfig.horizontalBlock * 4,
                                          fontWeight: FontWeight.w500)),
                                  // Text(subTitle,
                                  //     style: TextStyle(
                                  //         fontSize: SizeConfig.horizontalBlock *
                                  //             3.25))
                                ]),
                          ),
                          (dataPenjualState.length >= 1 &&
                                  shownView == 'produk')
                              ? InkWell(
                                  onTap: () {
                                    setState(() {
                                      shownView = 'penjual';
                                    });
                                  },
                                  child: Image.asset(
                                    ClientPath.assetsImg + '/store.png',
                                    width: 35.0,
                                    height: 35.0,
                                  ),
                                )
                              : SizedBox(),
                          (dataPenjualState.length >= 1 &&
                                  shownView == 'penjual')
                              ? InkWell(
                                  onTap: () {
                                    setState(() {
                                      shownView = 'produk';
                                    });
                                  },
                                  child: Image.asset(
                                    ClientPath.assetsImg + '/produk.png',
                                    width: 35.0,
                                    height: 35.0,
                                  ),
                                )
                              : SizedBox()
                        ],
                      )),
                  Padding(
                    padding:
                        EdgeInsets.symmetric(vertical: 10.0, horizontal: 15.0),
                    child: Pencarian(
                        onSearch: (String string) {
                          String stringLC = string.toLowerCase();

                          if (shownView == 'produk') {
                            List filteredDataProduk =
                                dataProdukImmutableState.where((produk) {
                              String namaProduk = produk['nama'];
                              String namaProdukLC = namaProduk.toLowerCase();

                              return namaProdukLC.indexOf(stringLC) >= 0;
                            }).toList();

                            setState(() {
                              pencarian = stringLC;
                              dataProdukState = filteredDataProduk;
                            });
                          } else {
                            List filteredDataPenjual =
                                dataPenjualImmutableState.where((penjual) {
                              String namaPenjual = penjual['namaToko'];
                              String namaPenjualLC = namaPenjual.toLowerCase();

                              return namaPenjualLC.indexOf(stringLC) >= 0;
                            }).toList();

                            setState(() {
                              pencarian = stringLC;
                              dataPenjualState = filteredDataPenjual;
                            });
                          }
                        },
                        onClose: () {
                          setState(() {
                            pencarian = '';
                            if (shownView == 'produk') {
                              dataProdukState = dataProdukImmutableState;
                            } else {
                              dataPenjualState = dataPenjualImmutableState;
                            }
                          });
                        },
                        hint: 'Cari Produk dengan Nama',
                        text: pencarian),
                  ),
                  Expanded(
                    child: LayoutBuilder(builder: (context, constraints) {
                      //sebellumnya ada refreshindicator tapi gak work
                      return (dataProdukState.length >= 1)
                          ? Padding(
                              padding: _horizontalPadding,
                              child: GridView.count(
                                  padding: EdgeInsets.zero,
                                  childAspectRatio: 0.60,
                                  crossAxisCount: 2,
                                  crossAxisSpacing:
                                      SizeConfig.horizontalBlock * 1.5,
                                  children: dataToShow),
                            )
                          : Padding(
                              padding: EdgeInsets.symmetric(
                                  horizontal: SizeConfig.horizontalBlock * 15),
                              child:
                                  ListView(padding: EdgeInsets.zero, children: [
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
                                                      30),
                                          SizedBox(height: 5),
                                          Text(
                                              'Tidak ada produk. Yuk mak, cari - cari lagi produk lainnya dan dapatkan produk yang mamak cari!',
                                              textAlign: TextAlign.center,
                                              style: TextStyle(
                                                  height: 1.25,
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.25))
                                        ]))
                              ]),
                            );
                    }),
                  )
                ],
              )
            : Center(
                child: SizedBox(
                    width: 25,
                    height: 25,
                    child: CircularProgressIndicator(strokeWidth: 1.5))),
        bottomNavigationBar: Footer());
  }
}
