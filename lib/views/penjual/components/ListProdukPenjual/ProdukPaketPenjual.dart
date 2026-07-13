import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/penjual/TambahProduk.dart';
import 'package:laukpauk/views/penjual/components/ListProdukPenjual/ProdukPaketPenjualItem.dart';

class ProdukPaketPenjual extends StatefulWidget {
  _ProdukPaketPenjualState createState() => _ProdukPaketPenjualState();
}

class _ProdukPaketPenjualState extends State<ProdukPaketPenjual> {
  int reload = 0;

  Future<List> getProdukPaketPenjual() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/getPaketPenjual?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      List paketPenjual = dataObject['paketPenjual'];

      return paketPenjual;
    }

    return [];
  }

  Widget build(BuildContext context) {
    return FutureBuilder<List>(
        future: getProdukPaketPenjual(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List produkPaketan = snapshot.data!;
              List<Widget> produkPaketPenjualWidget = [];
              if (produkPaketan.length >= 1) {
                produkPaketPenjualWidget = produkPaketan
                    .map((produkItem) => ProdukPaketPenjualItem(produkItem,
                            hapusPaketCallback: (statusHapusPaket) {
                          if (statusHapusPaket != null) {
                            if (statusHapusPaket) {
                              setState(() {
                                reload++;
                              });
                            } else {
                              SnackBar failedHapusProduk = SnackBar(
                                  duration: Duration(seconds: 3),
                                  content: Text(
                                      'Gagal menghapus produk paket. Silahkan coba lagi !',
                                      style: TextStyle(
                                          fontSize: SizeConfig.horizontalBlock *
                                              2.5)));

                              ScaffoldMessenger.of(context)
                                  .showSnackBar(failedHapusProduk);
                            }
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
                                        fontSize:
                                            SizeConfig.horizontalBlock * 2.5)));
                            // Scaffold.of(context).showSnackBar(failedEditProduk);

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
                    child: (produkPaketan.length >= 1)
                        ? ListView(
                            padding: EdgeInsets.zero,
                            children: produkPaketPenjualWidget)
                        : Container(
                            constraints: BoxConstraints(
                                minHeight: constraints.maxHeight),
                            child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  Image.asset(
                                      ClientPath.assetsImg + '/empty.png',
                                      width: SizeConfig.horizontalBlock * 25),
                                  SizedBox(height: 5),
                                  Text(
                                      'Anda belum memiliki produk PAKETAN apapun.',
                                      style: TextStyle(
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3)),
                                  TextButton(
                                      onPressed: () async {
                                        bool? statusTambahProduk =
                                            await Navigator.push(
                                                context,
                                                MaterialPageRoute(
                                                    builder: (_) =>
                                                        TambahProduk(
                                                            null, 'satuan')));
                                        if (statusTambahProduk != null) {
                                          if (statusTambahProduk) {
                                            setState(() {
                                              reload++;
                                            });
                                          } else {
                                            SnackBar failedAddProduk = SnackBar(
                                                duration: Duration(seconds: 3),
                                                content: Text(
                                                    'Gagal menambah data produk baru. Silahkan coba lagi !',
                                                    style: TextStyle(
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            2.5)));
                                            // Scaffold.of(context)
                                            //     .showSnackBar(failedAddProduk);

                                            ScaffoldMessenger.of(context)
                                                .showSnackBar(failedAddProduk);
                                          }
                                        }
                                      },
                                      child: Text('Tambah Produk Baru',
                                          style: TextStyle(
                                              color: CustomColors.green)))
                                ]),
                          ));
              });
            }
          }
          return Padding(
            padding: EdgeInsets.symmetric(vertical: 25),
            child: Center(
                child: SizedBox(
                    width: 15,
                    height: 15,
                    child: CircularProgressIndicator(strokeWidth: 1.5))),
          );
        });
  }
}
