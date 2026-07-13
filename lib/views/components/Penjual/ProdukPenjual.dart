import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/components/ProdukDiDalamPasar/ProdukDiDalamPasarItem.dart';

import 'package:laukpauk/global-components/Pencarian.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/models/ProdukModel.dart';

class ProdukPenjual extends StatefulWidget {
  final String idPenjual;
  ProdukPenjual({required this.idPenjual});

  _ProdukPenjualState createState() => _ProdukPenjualState();
}

class _ProdukPenjualState extends State<ProdukPenjual> {
  bool isReadyToShow = false;
  List produkPenjualState = [];
  List produkPenjualImmutableState = [];
  String pencarian = '';

  Future<List> getProdukPenjual() async {
    String idPenjual = widget.idPenjual.toString();
    String getProductEntities =
        'nama, foto, id, namaToko, namaKategoriProduk, idPenjual, stokAvailable';

    String _params = await HTTPRequest.getQueryString(
        params: 'getProductEntities=' + getProductEntities);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Penjual/getProdukPenjual/' +
        idPenjual +
        '?' +
        _params);

    http.Response httpResponse = await http.get(_url);
    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      List produkPenjual = dataObject['produkPenjual'];

      return produkPenjual;
    }
    return [];
  }

  void initialization() async {
    List produkPenjual = await getProdukPenjual();

    setState(() {
      isReadyToShow = true;
      produkPenjualState = produkPenjual;
      produkPenjualImmutableState = produkPenjual;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    List<Widget> produkPenjualWidget = (isReadyToShow)
        ? (produkPenjualState.length >= 1)
            ? produkPenjualState.map((produk) {
                return ProdukInThisPasarItem(produk, () async {
                  Map<String, dynamic> adjdSet =
                      await HeaderModel.adjd(context);
                  String? latitude = adjdSet['latitude'];
                  String? longitude = adjdSet['longitude'];
                  String? jadwalDeliveryCode = adjdSet['kodeJadwalDelivery'];

                  bool isADJDCompleted = false;
                  if (latitude != null &&
                      longitude != null &&
                      jadwalDeliveryCode != null) {
                    isADJDCompleted = true;

                    await ProdukModel.showSatuanProduk(context, produk);
                  }
                  Provider.of<HeaderViewModel>(context, listen: false)
                      .isADJDSet = isADJDCompleted;
                });
              }).toList()
            : []
        : [];

    return (isReadyToShow)
        ? (produkPenjualState.length >= 1)
            ? Column(
                children: [
                  Padding(
                    padding: EdgeInsets.only(top: 5.0, bottom: 5.0),
                    child: Pencarian(
                        onSearch: (String string) {
                          String stringLC = string.toLowerCase();
                          List produkPenjualFiltered =
                              produkPenjualImmutableState.where((produk) {
                            String namaProduk = produk['nama'];
                            String namaProdukLC = namaProduk.toLowerCase();

                            return namaProdukLC.indexOf(stringLC) >= 0;
                          }).toList();

                          setState(() {
                            produkPenjualState = produkPenjualFiltered;
                            pencarian = stringLC;
                          });
                        },
                        onClose: () {
                          setState(() {
                            produkPenjualState = produkPenjualImmutableState;
                            pencarian = '';
                          });
                        },
                        hint: 'Cari Produk di Sini ...',
                        text: pencarian),
                  ),
                  SizedBox(height: 5.0),
                  Expanded(
                    child: GridView.count(
                        padding: EdgeInsets.only(top: 0),
                        crossAxisCount: 3,
                        childAspectRatio: 0.5,
                        children: produkPenjualWidget),
                  ),
                ],
              )
            : Column(children: [
                Padding(
                  padding: EdgeInsets.only(top: 5.0, bottom: 5.0),
                  child: Pencarian(
                      onSearch: (String string) {
                        String stringLC = string.toLowerCase();
                        List produkPenjualFiltered =
                            produkPenjualImmutableState.where((produk) {
                          String namaProduk = produk['nama'];
                          String namaProdukLC = namaProduk.toLowerCase();

                          return namaProdukLC.indexOf(stringLC) >= 0;
                        }).toList();

                        setState(() {
                          produkPenjualState = produkPenjualFiltered;
                          pencarian = stringLC;
                        });
                      },
                      onClose: () {
                        setState(() {
                          produkPenjualState = produkPenjualImmutableState;
                          pencarian = '';
                        });
                      },
                      hint: 'Cari Produk di Sini',
                      text: pencarian),
                ),
                SizedBox(height: 15.0),
                Expanded(
                  child: LayoutBuilder(builder: (context, constraints) {
                    return Container(
                        constraints:
                            BoxConstraints(minHeight: constraints.maxHeight),
                        child: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Image.asset(ClientPath.assetsImg + '/empty.png',
                                  width: SizeConfig.horizontalBlock * 25),
                              Padding(
                                  padding: EdgeInsets.only(top: 5),
                                  child: Text('Tidak Ada Produk',
                                      style: TextStyle(
                                          color: CustomColors.textMuted,
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.5)))
                            ]));
                  }),
                )
              ])
        : Loading();
  }
}
