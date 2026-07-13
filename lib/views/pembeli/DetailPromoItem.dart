import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/global-components/Yuhu.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/models/AuthModel.dart';

import 'package:laukpauk/views/pembeli/components/DetailPromo/NamaPenjual.dart';
import 'package:laukpauk/views/pembeli/components/DetailPromo/NamaProduk.dart';
import 'package:laukpauk/views/pembeli/components/DetailPromo/NamaPromo.dart';

class DetailPromoItem extends StatefulWidget {
  final String idPromoItem;
  DetailPromoItem(this.idPromoItem);

  DetailPromoItemState createState() => DetailPromoItemState();
}

class DetailPromoItemState extends State<DetailPromoItem> {
  Future<dynamic> getDetailPromoItem() async {
    bool isLogin = await AuthModel.isLogin();
    int currentUserIDInt = await LocalStorage.getIdPenjualFromActiveUser();
    String currentUserID = currentUserIDInt.toString();

    String idPromoItem = widget.idPromoItem;
    String select = 'namaPromo, statusPromo, catatan, idPromo, diskon';
    String _params = await HTTPRequest.getQueryString(params: 'select=$select');

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Promo/getPromoItem/$idPromoItem?' + _params);

    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      dynamic detailPromoItem = dataObject['promoItem'];

      Map<String, dynamic> data = {
        'isLogin': isLogin,
        'currentUserID': currentUserID,
        'promoItem': detailPromoItem
      };

      return data;
    }

    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
      backgroundColor: CustomColors.nearlyWhite,
      body: Column(mainAxisAlignment: MainAxisAlignment.start, children: [
        Header(),
        Expanded(
            child: FutureBuilder(
                future: getDetailPromoItem(),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    dynamic data = snapshot.data;
                    if (data != false) {
                      Map<String, dynamic> detailPromoItem = data['promoItem'];

                      Map<String, dynamic> _detailPromo =
                          detailPromoItem['detailPromo'];

                      String idPromo = detailPromoItem['idPromo'];
                      String statusPromo = detailPromoItem['statusPromo'];
                      String currentUserIDString = data['currentUserID'];

                      String idPenjual = '',
                          idProduk = '',
                          keterangan = '',
                          fotoProduk = '',
                          namaToko = '',
                          namaProduk = '',
                          namaKategoriProduk = '',
                          _catatanPromoProduk = '';

                      _catatanPromoProduk = detailPromoItem['catatan'];

                      dynamic detailProduk = detailPromoItem['detailProduk'];
                      if (detailProduk != false) {
                        idProduk = detailProduk['id'];
                        namaProduk = detailProduk['nama'];
                        fotoProduk = detailProduk['foto'];
                        namaKategoriProduk = detailProduk['namaKategoriProduk'];

                        namaToko = detailProduk['namaToko'];
                        idPenjual = detailProduk['idPenjual'];
                        keterangan = detailProduk['keterangan'];
                      }

                      dynamic detailSatuan = detailPromoItem['detailSatuan'];
                      String idSatuan =
                          (detailSatuan != false) ? detailSatuan['id'] : '0';

                      Map<String, dynamic> detailPenjual = {
                        'namaToko': namaToko,
                        'idToko': idPenjual
                      };

                      Map<String, dynamic> dataProduk = {
                        'namaProduk': namaProduk,
                        'namaKategoriProduk': namaKategoriProduk,
                        'idProduk': idProduk,
                        'idPenjual': idPenjual,
                        'idSatuan': idSatuan,
                        'idPromo': idPromo,
                        'statusPromo': statusPromo
                      };

                      return RefreshIndicator(
                        onRefresh: () async {
                          setState(() {});
                        },
                        child: ListView(
                            shrinkWrap: true,
                            padding: EdgeInsets.zero,
                            children: [
                              Container(
                                width: SizeConfig.horizontalBlock * 100,
                                height: SizeConfig.horizontalBlock * 60,
                                decoration: BoxDecoration(
                                    image: DecorationImage(
                                        fit: BoxFit.cover,
                                        image: NetworkImage(ServerPath.baseURL +
                                            '/' +
                                            ServerPath.uploadGambarProduk +
                                            '/' +
                                            fotoProduk))),
                              ),
                              Padding(
                                  padding: EdgeInsets.symmetric(
                                      horizontal: 2.5, vertical: 5),
                                  child: Padding(
                                    padding: EdgeInsets.symmetric(
                                        horizontal: 15, vertical: 15),
                                    child: Column(
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        NamaProduk(
                                            dataProduk, currentUserIDString,
                                            (statusEditProduk) {
                                          if (statusEditProduk != null) {
                                            String text = (statusEditProduk)
                                                ? 'Berhasil mengubah data produk'
                                                : 'Gagal mengubah data produk !';
                                            SnackBar snackBar = SnackBar(
                                                content: Text(text),
                                                duration: Duration(seconds: 2));

                                            ScaffoldMessenger.of(context)
                                                .showSnackBar(snackBar);
                                            if (statusEditProduk) {
                                              setState(() {});
                                            }
                                          }
                                        }),
                                        SizedBox(height: 2.5),
                                        NamaPenjual(detailPenjual),
                                        SizedBox(height: 15.0),
                                        NamaPromo(
                                            detailPromoItem: detailPromoItem,
                                            detailPromo: _detailPromo,
                                            detailSatuan: detailSatuan),
                                        Yuhu(),
                                        SizedBox(height: 15.0),
                                        Column(
                                            mainAxisAlignment:
                                                MainAxisAlignment.start,
                                            crossAxisAlignment:
                                                CrossAxisAlignment.start,
                                            children: [
                                              Text('Deskripsi Produk',
                                                  style: TextStyle(
                                                      color: CustomColors
                                                          .textMuted,
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          4,
                                                      fontWeight:
                                                          FontWeight.w700)),
                                              SizedBox(height: 5),
                                              Text(
                                                  (keterangan.length >= 1)
                                                      ? keterangan
                                                      : 'Tidak ada deskripsi produk',
                                                  style: TextStyle(
                                                      fontStyle: (keterangan
                                                                  .length >=
                                                              1)
                                                          ? null
                                                          : FontStyle.italic,
                                                      color: CustomColors
                                                          .textMuted,
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3)),
                                              SizedBox(height: 10.0),
                                              Text('Catatan Promo Produk',
                                                  style: TextStyle(
                                                      color: CustomColors
                                                          .textMuted,
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          4,
                                                      fontWeight:
                                                          FontWeight.w700)),
                                              SizedBox(height: 5),
                                              Text(
                                                  (_catatanPromoProduk.length >=
                                                          1)
                                                      ? _catatanPromoProduk
                                                      : 'Tidak ada deskripsi produk',
                                                  style: TextStyle(
                                                      fontStyle:
                                                          (_catatanPromoProduk
                                                                      .length >=
                                                                  1)
                                                              ? null
                                                              : FontStyle
                                                                  .italic,
                                                      color: CustomColors
                                                          .textMuted,
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          3))
                                            ])
                                      ],
                                    ),
                                  ))
                            ]),
                      );
                    } else {
                      return LayoutBuilder(builder: (context, constraints) {
                        return RefreshIndicator(
                          onRefresh: () async {
                            setState(() {});
                          },
                          child: ListView(
                              shrinkWrap: true,
                              padding: EdgeInsets.zero,
                              children: [
                                Container(
                                  constraints: BoxConstraints(
                                      minHeight: constraints.maxHeight),
                                  child: Column(
                                      mainAxisAlignment:
                                          MainAxisAlignment.center,
                                      children: [
                                        Image.asset(
                                          ClientPath.assetsImg + '/empty.png',
                                          width:
                                              SizeConfig.horizontalBlock * 35.0,
                                        ),
                                        SizedBox(height: 20),
                                        Text('Detail Produk Tidak Ditemukan',
                                            style: TextStyle(
                                                color: CustomColors.textMuted,
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3.25))
                                      ]),
                                )
                              ]),
                        );
                      });
                    }
                  } else {
                    return Loading();
                  }
                })),
      ]),
      bottomNavigationBar: Footer(),
    );
  }
}
