import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/global-components/Yuhu.dart';
import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

import 'package:laukpauk/views/pembeli/components/DetailProduk/ProdukPenjualLainnya.dart';
import 'package:laukpauk/views/Penjual.dart';
import 'package:laukpauk/models/ProdukModel.dart';
import 'package:laukpauk/views/penjual/TambahProduk.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/models/AuthModel.dart';

class DetailProduk extends StatefulWidget {
  final String id;
  DetailProduk(this.id);

  DetailProdukState createState() => DetailProdukState();
}

class DetailProdukState extends State<DetailProduk> {
  int hargaTerpilih = 0;

  bool _isButtonBeliBusy = false;

  Future<dynamic> getDetailProduk() async {
    bool isLogin = await AuthModel.isLogin();
    int currentUserIDInt = await LocalStorage.getIdPenjualFromActiveUser();
    String currentUserID = currentUserIDInt.toString();

    String idProduk = widget.id;

    String _params =
        await HTTPRequest.getQueryString(params: 'idProduk=$idProduk');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Produk/detailProduk?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      Map<String, dynamic> data = {
        'isLogin': isLogin,
        'currentUserID': currentUserID,
        'detailProduk': dataObject['detailProduk']
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
                  future: getDetailProduk(),
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      if (snapshot.data != null) {
                        dynamic data = snapshot.data;
                        if (data != false) {
                          Map<String, dynamic> detailProduk =
                              data['detailProduk'];
                          String namaKategoriProduk =
                              (detailProduk['namaKategoriProduk'] != null)
                                  ? detailProduk['namaKategoriProduk']
                                  : 'Kategori Produk Tidak Diketahui';

                          String _keterangan = detailProduk['keterangan'];

                          String idProduk = detailProduk['id'];
                          String foto = detailProduk['foto'];
                          String fotoLC = foto.toLowerCase();

                          String currentUserID = data['currentUserID'];
                          bool isItMyProduct =
                              detailProduk['idPenjual'] == currentUserID;

                          String defaultProdukImg = DefaultImage.defaultProduk;
                          String defaultProdukImgLC =
                              defaultProdukImg.toLowerCase();

                          bool isDefaultImage = defaultProdukImgLC == fotoLC;

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
                                            fit: (isDefaultImage)
                                                ? BoxFit.contain
                                                : BoxFit.cover,
                                            image: NetworkImage(ServerPath
                                                    .baseURL +
                                                '/' +
                                                ServerPath.uploadGambarProduk +
                                                '/' +
                                                foto))),
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
                                            Row(
                                                crossAxisAlignment:
                                                    CrossAxisAlignment.start,
                                                mainAxisAlignment:
                                                    MainAxisAlignment
                                                        .spaceBetween,
                                                children: [
                                                  Expanded(
                                                    child: Column(
                                                      crossAxisAlignment:
                                                          CrossAxisAlignment
                                                              .start,
                                                      children: [
                                                        Text(
                                                            detailProduk['nama']
                                                                .toUpperCase(),
                                                            style: TextStyle(
                                                                color:
                                                                    CustomColors
                                                                        .green,
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    4.5,
                                                                fontWeight:
                                                                    FontWeight
                                                                        .w700)),
                                                        SizedBox(height: 5),
                                                        Text(
                                                            namaKategoriProduk
                                                                .toUpperCase(),
                                                            style: TextStyle(
                                                                fontStyle: (detailProduk[
                                                                            'namaKategoriProduk'] ==
                                                                        null)
                                                                    ? FontStyle
                                                                        .italic
                                                                    : null,
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    3,
                                                                color: CustomColors
                                                                    .textMuted)),
                                                        SizedBox(height: 15),
                                                        Row(
                                                            mainAxisAlignment:
                                                                MainAxisAlignment
                                                                    .start,
                                                            children: [
                                                              Image.asset(
                                                                  ClientPath
                                                                          .assetsImg +
                                                                      '/store.png',
                                                                  width: SizeConfig
                                                                          .horizontalBlock *
                                                                      7.5),
                                                              SizedBox(
                                                                  width: 10),
                                                              Expanded(
                                                                child: Column(
                                                                    crossAxisAlignment:
                                                                        CrossAxisAlignment
                                                                            .start,
                                                                    children: [
                                                                      SingleChildScrollView(
                                                                        scrollDirection:
                                                                            Axis.horizontal,
                                                                        child: Row(
                                                                            mainAxisAlignment:
                                                                                MainAxisAlignment.start,
                                                                            children: [
                                                                              Text('Penjual', style: TextStyle(color: CustomColors.textMuted, fontSize: SizeConfig.horizontalBlock * 3.5)),
                                                                              SizedBox(width: 5),
                                                                              InkWell(
                                                                                onTap: () {
                                                                                  Navigator.push(context, MaterialPageRoute(builder: (_) => Penjual(id: detailProduk['idPenjual'])));
                                                                                },
                                                                                child: Text((detailProduk['namaToko'].length >= 1) ? detailProduk['namaToko'] : 'Penjual Tidak Diketahui', style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5, color: CustomColors.textInfo, fontWeight: FontWeight.w600)),
                                                                              )
                                                                            ]),
                                                                      ),
                                                                      Text(
                                                                          'Klik nama penjual untuk melihat seluruh produk penjual',
                                                                          style:
                                                                              TextStyle(fontSize: SizeConfig.horizontalBlock * 2.5)),
                                                                    ]),
                                                              )
                                                            ])
                                                      ],
                                                    ),
                                                  ),
                                                  SizedBox(width: 10),
                                                  CustomButton.button(
                                                      context,
                                                      (isItMyProduct)
                                                          ? 'Edit'
                                                          : 'Beli', () async {
                                                    if (!isItMyProduct) {
                                                      bool isADJDSet =
                                                          await LocalStorage
                                                              .isAlamatDeliveryDanJadwalDeliverySet();
                                                      if (isADJDSet) {
                                                        setState(() {
                                                          _isButtonBeliBusy =
                                                              true;
                                                        });
                                                        await ProdukModel
                                                            .showSatuanProduk(
                                                                context,
                                                                detailProduk);
                                                        setState(() {
                                                          _isButtonBeliBusy =
                                                              false;
                                                        });
                                                      } else {
                                                        bool? tentukanADJD =
                                                            await ProdukModel
                                                                .adjdNotSetAlert(
                                                                    context);
                                                        if (tentukanADJD !=
                                                                null &&
                                                            tentukanADJD) {
                                                          Map<String, dynamic>
                                                              adjdSet =
                                                              await HeaderModel
                                                                  .adjd(
                                                                      context);
                                                          String? latitude =
                                                              adjdSet[
                                                                  'latitude'];
                                                          String? longitude =
                                                              adjdSet[
                                                                  'longitude'];
                                                          String?
                                                              kodeJadwalDelivery =
                                                              adjdSet[
                                                                  'kodeJadwalDelivery'];

                                                          bool isADJDSet =
                                                              false;
                                                          if (latitude !=
                                                                  null &&
                                                              longitude !=
                                                                  null &&
                                                              kodeJadwalDelivery !=
                                                                  null) {
                                                            isADJDSet = true;
                                                            ProdukModel
                                                                .showSatuanProduk(
                                                                    context,
                                                                    detailProduk);
                                                          }
                                                          Provider.of<HeaderViewModel>(
                                                                      context,
                                                                      listen: false)
                                                                  .isADJDSet =
                                                              isADJDSet;
                                                        }
                                                      }
                                                    } else {
                                                      bool? statusEditProduk =
                                                          await Navigator.push(
                                                              context,
                                                              MaterialPageRoute(
                                                                  builder: (_) =>
                                                                      TambahProduk(
                                                                          idProduk,
                                                                          'satuan')));
                                                      if (statusEditProduk !=
                                                          null) {
                                                        String text = (statusEditProduk)
                                                            ? 'Berhasil mengubah data produk'
                                                            : 'Gagal mengubah data produk !';
                                                        SnackBar snackBar =
                                                            SnackBar(
                                                                content:
                                                                    Text(text),
                                                                duration:
                                                                    Duration(
                                                                        seconds:
                                                                            2));
                                                        // Scaffold.of(context)
                                                        //     .showSnackBar(snackBar);
                                                        ScaffoldMessenger.of(
                                                                context)
                                                            .showSnackBar(
                                                                snackBar);
                                                        if (statusEditProduk) {
                                                          setState(() {});
                                                        }
                                                      }
                                                    }
                                                  },
                                                      isBusy: _isButtonBeliBusy,
                                                      color: (!isItMyProduct)
                                                          ? CustomButton
                                                              .themeColor
                                                          : CustomButton
                                                              .warningColor,
                                                      isPill: true)
                                                ]),
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
                                                      (_keterangan.isNotEmpty)
                                                          ? _keterangan
                                                          : 'Deskripsi tidak ada',
                                                      style: TextStyle(
                                                          fontStyle: (_keterangan
                                                                  .isNotEmpty)
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
                                      )),
                                  Padding(
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 15, vertical: 10),
                                      child: Column(
                                          mainAxisAlignment:
                                              MainAxisAlignment.start,
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Text('Produk Lainnya',
                                                textAlign: TextAlign.left,
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        3.75,
                                                    fontWeight:
                                                        FontWeight.w700)),
                                            SizedBox(height: 10),
                                            ProdukPenjualLainnya(
                                                currentProduct: widget.id)
                                          ]))
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
                                              ClientPath.assetsImg +
                                                  '/empty.png',
                                              width: 200,
                                            ),
                                            SizedBox(height: 20),
                                            Text(
                                                'Detail Produk Tidak Ditemukan',
                                                style: TextStyle(
                                                    color:
                                                        CustomColors.textDanger,
                                                    fontSize: 16,
                                                    fontWeight:
                                                        FontWeight.w600))
                                          ]),
                                    )
                                  ]),
                            );
                          });
                        }
                      }
                    }
                    return Loading();
                  })),
        ]),
        bottomNavigationBar: Footer());
  }
}
