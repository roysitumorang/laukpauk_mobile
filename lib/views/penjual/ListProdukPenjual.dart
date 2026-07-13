import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/penjual/components/ListProdukPenjual/ListProdukPenjualItem.dart';
import 'package:laukpauk/views/penjual/TambahProduk.dart';
import 'package:laukpauk/views/penjual/components/ListProdukPenjual/ProdukPaketPenjual.dart';

class ListProdukPenjual extends StatefulWidget {
  _ListProdukPenjualState createState() => _ListProdukPenjualState();
}

class _ListProdukPenjualState extends State<ListProdukPenjual>
    with TickerProviderStateMixin {
  int reload = 0;
  int jenisProdukSelected = 0;

  Future<List> getProdukPenjual() async {
    String getProductEntities =
        'id, nama, foto, namaKategoriProduk, keterangan';
    String _params = await HTTPRequest.getQueryString(
        params: 'getProductEntities=$getProductEntities');

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/getProdukPenjual?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      List produkPenjual = dataObject['produkPenjual'];

      return produkPenjual;
    }

    return [];
  }

  Widget build(BuildContext context) {
    return Scaffold(
        backgroundColor: CustomColors.aliceBlue,
        body: Column(mainAxisAlignment: MainAxisAlignment.start, children: [
          Header(),
          Padding(
              padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
              child: Card(
                elevation: 5,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10)),
                child: Padding(
                    padding: EdgeInsets.symmetric(horizontal: 15, vertical: 10),
                    child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text('Produk Anda',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5,
                                      fontWeight: FontWeight.w700)),
                              InkWell(
                                onTap: () async {
                                  bool? statusTambahProduk =
                                      await Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                              builder: (_) => TambahProduk(
                                                  null, 'satuan')));
                                  if (statusTambahProduk != null) {
                                    if (statusTambahProduk) {
                                      setState(() {
                                        reload++;
                                      });
                                    } else {
                                      SnackBar failedTambahProduk = SnackBar(
                                          duration: Duration(seconds: 3),
                                          content: Text(
                                              'Anda gagal menambahkan produk baru. Silahkan coba lagi !',
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      2.5)));
                                      // Scaffold.of(context)
                                      //     .showSnackBar(failedTambahProduk);
                                      ScaffoldMessenger.of(context)
                                          .showSnackBar(failedTambahProduk);
                                    }
                                  }
                                },
                                child: Text('Tambah Produk Baru',
                                    style: TextStyle(
                                        color: CustomColors.textPrimary,
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3,
                                        fontWeight: FontWeight.w700)),
                              ),
                            ],
                          ),
                          SizedBox(height: 5),
                          Text(
                              'Silahkan atur produk anda. Anda bisa menambah, mengubah dan menghapus produk - produk anda.',
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 3)),
                        ])),
              )),
          Padding(
            padding: EdgeInsets.only(bottom: 5),
            child: TabBar(
                isScrollable: true,
                onTap: (value) {
                  setState(() {
                    jenisProdukSelected = value;
                  });
                },
                unselectedLabelColor: CustomColors.textMuted,
                indicatorColor: CustomColors.green,
                labelColor: CustomColors.green,
                controller: TabController(
                    length: 2, vsync: this, initialIndex: jenisProdukSelected),
                tabs: [
                  Tab(child: Text('Produk Satuan')),
                  Tab(child: Text('Produk Paketan'))
                ]),
          ),
          Expanded(
            child: (jenisProdukSelected == 0)
                ? FutureBuilder<List>(
                    future: getProdukPenjual(),
                    builder: (context, snapshot) {
                      if (snapshot.hasData) {
                        if (snapshot.data != null) {
                          List produkPenjual = snapshot.data!;
                          List<Widget> produkPenjualWidget = [];
                          if (produkPenjual.length >= 1) {
                            produkPenjualWidget = produkPenjual
                                .map((produkItem) => ListProdukPenjualItem(
                                        produkItem, hapusProdukCallback:
                                            (statusHapusProduk) {
                                      if (statusHapusProduk) {
                                        setState(() {
                                          reload++;
                                        });
                                      } else {
                                        SnackBar failedHapusProduk = SnackBar(
                                            duration: Duration(seconds: 3),
                                            content: Text(
                                                'Gagal menghapus produk. Silahkan coba lagi !',
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        2.5)));
                                        // Scaffold.of(context)
                                        //     .showSnackBar(failedHapusProduk);

                                        ScaffoldMessenger.of(context)
                                            .showSnackBar(failedHapusProduk);
                                      }
                                    }, editProdukCallback: (statusEditProduk) {
                                      if (statusEditProduk) {
                                        setState(() {
                                          reload++;
                                        });
                                      } else {
                                        SnackBar failedEditProduk = SnackBar(
                                            duration: Duration(seconds: 3),
                                            content: Text(
                                                'Gagal memperbaharui data produk. Silahkan coba lagi !',
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        2.5)));
                                        // Scaffold.of(context)
                                        //     .showSnackBar(failedEditProduk);
                                        ScaffoldMessenger.of(context)
                                            .showSnackBar(failedEditProduk);
                                      }
                                    }))
                                .toList();
                          }

                          return LayoutBuilder(builder: (context, constraints) {
                            return RefreshIndicator(
                              onRefresh: () async {
                                setState(() {});
                              },
                              child: (produkPenjual.length >= 1)
                                  ? ListView(
                                      padding: EdgeInsets.zero,
                                      children: produkPenjualWidget)
                                  : ListView(
                                      padding: EdgeInsets.zero,
                                      children: [
                                        Container(
                                          constraints: BoxConstraints(
                                              minHeight: constraints.maxHeight),
                                          child: Padding(
                                            padding: EdgeInsets.symmetric(
                                                vertical: 15),
                                            child: Column(
                                                mainAxisAlignment:
                                                    MainAxisAlignment.center,
                                                children: [
                                                  Image.asset(
                                                      ClientPath.assetsImg +
                                                          '/empty.png',
                                                      width: SizeConfig
                                                              .horizontalBlock *
                                                          25),
                                                  SizedBox(height: 5),
                                                  Text(
                                                      'Anda belum memiliki produk SATUAN apapun.',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3)),
                                                  TextButton(
                                                      onPressed: () async {
                                                        bool?
                                                            statusTambahProduk =
                                                            await Navigator.push(
                                                                context,
                                                                MaterialPageRoute(
                                                                    builder: (_) =>
                                                                        TambahProduk(
                                                                            null,
                                                                            'satuan')));
                                                        if (statusTambahProduk !=
                                                            null) {
                                                          if (statusTambahProduk) {
                                                            setState(() {
                                                              reload++;
                                                            });
                                                          } else {
                                                            SnackBar failedAddProduk = SnackBar(
                                                                duration:
                                                                    Duration(
                                                                        seconds:
                                                                            3),
                                                                content: Text(
                                                                    'Gagal menambah data produk baru. Silahkan coba lagi !',
                                                                    style: TextStyle(
                                                                        fontSize:
                                                                            SizeConfig.horizontalBlock *
                                                                                2.5)));
                                                            // Scaffold.of(context)
                                                            //     .showSnackBar(
                                                            //         failedAddProduk);

                                                            ScaffoldMessenger
                                                                    .of(context)
                                                                .showSnackBar(
                                                                    failedAddProduk);
                                                          }
                                                        }
                                                      },
                                                      child: Text(
                                                          'Tambah Produk Baru',
                                                          style: TextStyle(
                                                              color:
                                                                  CustomColors
                                                                      .green)))
                                                ]),
                                          ),
                                        ),
                                      ],
                                    ),
                            );
                          });
                        }
                      }
                      return Padding(
                        padding: EdgeInsets.symmetric(vertical: 25),
                        child: Center(
                            child: SizedBox(
                                width: 25,
                                height: 25,
                                child: CircularProgressIndicator(
                                    strokeWidth: 1.5))),
                      );
                    })
                : ProdukPaketPenjual(),
          )
        ]),
        bottomNavigationBar: Footer());
  }
}
