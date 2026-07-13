import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/Pencarian.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/models/KategoriProdukModel.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/SemuaKategoriProdukItem.dart';

class SemuaKategoriProduk extends StatefulWidget {
  _SemuaKategoriProdukState createState() => _SemuaKategoriProdukState();
}

class _SemuaKategoriProdukState extends State<SemuaKategoriProduk> {
  List kategoriProdukState = [];
  List kategoriProdukImmutableState = [];
  bool isReadyToShow = false;
  String pencarian = '';

  void initialization() async {
    List kategoriProduk = await KategoriProdukModel.getKategoriProduk();

    setState(() {
      isReadyToShow = true;
      kategoriProdukState = kategoriProduk;
      kategoriProdukImmutableState = kategoriProduk;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    List<Widget> kategoriProdukWidget = [];
    if (isReadyToShow) {
      if (kategoriProdukState.length >= 1) {
        kategoriProdukWidget = kategoriProdukState
            .map((kategori) => SemuaKategoriProdukItem(kategori))
            .toList();
      }
    }

    return Scaffold(
        backgroundColor: CustomColors.aliceBlue,
        appBar: AppBar(
            title: Text('Semua Kategori Produk'),
            backgroundColor: CustomColors.green),
        body: (isReadyToShow)
            ? Padding(
                padding: EdgeInsets.symmetric(horizontal: 7.5, vertical: 10.0),
                child: Column(children: [
                  Pencarian(
                      onSearch: (String string) {
                        String stringLC = string.toLowerCase();

                        List kategoriProdukFiltered =
                            kategoriProdukImmutableState
                                .where((kategoriProduk) {
                          String nama = kategoriProduk['nama'];
                          String namaLC = nama.toLowerCase();

                          return namaLC.indexOf(stringLC) >= 0;
                        }).toList();

                        setState(() {
                          pencarian = stringLC;
                          kategoriProdukState = kategoriProdukFiltered;
                        });
                      },
                      onClose: () {
                        setState(() {
                          pencarian = '';
                          kategoriProdukState = kategoriProdukImmutableState;
                        });
                      },
                      hint: 'Cari Kategori Produk di sini ..',
                      text: pencarian),
                  SizedBox(height: 7.5),
                  Expanded(
                    child: LayoutBuilder(builder: (context, constraints) {
                      return RefreshIndicator(
                        onRefresh: () async {
                          initialization();
                        },
                        child: (kategoriProdukState.length >= 1)
                            ? GridView.count(
                                padding: EdgeInsets.only(top: 0),
                                crossAxisCount: 3,
                                crossAxisSpacing: 5.0,
                                mainAxisSpacing: 5.0,
                                childAspectRatio: 1,
                                children: kategoriProdukWidget)
                            : ListView(
                                children: [
                                  Container(
                                      constraints: BoxConstraints(
                                          minHeight: constraints.maxHeight),
                                      child: Column(
                                          mainAxisAlignment:
                                              MainAxisAlignment.center,
                                          crossAxisAlignment:
                                              CrossAxisAlignment.center,
                                          children: [
                                            Image.asset(
                                                ClientPath.assetsImg +
                                                    '/empty.png',
                                                width:
                                                    SizeConfig.horizontalBlock *
                                                        30),
                                            SizedBox(height: 10),
                                            Text('Kategori Produk Tidak Ada',
                                                style: TextStyle(
                                                    color:
                                                        CustomColors.textMuted,
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        3.5))
                                          ]))
                                ],
                              ),
                      );
                    }),
                  )
                ]),
              )
            : Center(
                child: SizedBox(
                    width: 25.0,
                    height: 25.0,
                    child: CircularProgressIndicator(strokeWidth: 1.5))));
  }
}
