import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/views/Penjual.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

import 'package:laukpauk/views/pembeli/DetailProduk.dart';
import 'package:laukpauk/models/ProdukModel.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

class ListProdukItem extends StatelessWidget {
  final Map<String, dynamic> dataProduk;
  final double? widgetWidth;
  final void Function() deteksiADJD;
  ListProdukItem(this.dataProduk, this.deteksiADJD, {this.widgetWidth});

  Future<dynamic> getLowestPrice() async {
    String idProduk = dataProduk['id'];

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Produk/getLowestPriceOfThisProduct/$idProduk');
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      dynamic lowestPrice = dataObject['lowestPrice'];

      return lowestPrice;
    }

    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idPenjual = dataProduk['idPenjual'];
    String fotoProduk = dataProduk['foto'];
    String namaProduk = dataProduk['nama'];
    String idProduk = dataProduk['id'];
    String namaToko = dataProduk['namaToko'];

    bool isDefaultImage = (fotoProduk == DefaultImage.defaultProduk);

    return Container(
        width: widgetWidth,
        child: InkWell(
          onTap: () {
            Navigator.push(context, MaterialPageRoute(builder: (_) {
              return DetailProduk(idProduk);
            }));
          },
          child: Card(
              margin: EdgeInsets.only(bottom: 10, left: 2.5, right: 2.5),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              child: Column(
                children: [
                  Expanded(
                    child: LayoutBuilder(builder: (context, constraints) {
                      return Container(
                          margin: EdgeInsets.only(bottom: 15.0),
                          constraints: BoxConstraints(
                              minHeight: constraints.maxHeight,
                              minWidth: constraints.maxWidth),
                          decoration: BoxDecoration(
                              borderRadius: BorderRadius.only(
                                  topLeft: Radius.circular(10.0),
                                  topRight: Radius.circular(10.0)),
                              image: DecorationImage(
                                  image: NetworkImage(ServerPath.baseURL +
                                      '/' +
                                      ServerPath.uploadGambarProduk +
                                      '/compress/' +
                                      fotoProduk),
                                  fit: (isDefaultImage)
                                      ? BoxFit.contain
                                      : BoxFit.cover)));
                    }),
                  ),
                  Expanded(
                    child: SingleChildScrollView(
                      child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Padding(
                              padding: EdgeInsets.symmetric(horizontal: 15.0),
                              child: Text(namaProduk,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5,
                                      fontWeight: FontWeight.w700),
                                  textAlign: TextAlign.center),
                            ),
                            SizedBox(height: 2),
                            Padding(
                              padding: EdgeInsets.symmetric(horizontal: 15.0),
                              child: InkWell(
                                onTap: () {
                                  Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) =>
                                              Penjual(id: idPenjual)));
                                },
                                child: Text(
                                    (namaToko.length >= 1)
                                        ? namaToko
                                        : 'Penjual Tidak Diketahui',
                                    textAlign: TextAlign.center,
                                    style: TextStyle(
                                        color: CustomColors.textMuted,
                                        fontSize:
                                            SizeConfig.horizontalBlock * 2.75)),
                              ),
                            ),
                            SizedBox(height: 7.5),
                            Padding(
                              padding: EdgeInsets.symmetric(horizontal: 15.0),
                              child: FutureBuilder(
                                  future: getLowestPrice(),
                                  builder: (context, snapshot) {
                                    if (snapshot.hasData) {
                                      dynamic detailPaket = snapshot.data;
                                      if (detailPaket == false) {
                                        return Text(
                                            'Produk ini tidak memiliki daftar harga',
                                            textAlign: TextAlign.center,
                                            style: TextStyle(
                                                color: CustomColors.textMuted,
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        2.7));
                                      } else {
                                        int hargaBersih = int.parse(
                                            detailPaket['hargaBersih']);
                                        int hargaKotor =
                                            (detailPaket['harga'] != null)
                                                ? int.parse(
                                                    detailPaket['harga'])
                                                : 0;
                                        int diskon = (detailPaket['diskon'] !=
                                                null)
                                            ? int.parse(detailPaket['diskon'])
                                            : 0;
                                        double persentaseDiskon =
                                            (hargaKotor >= 1 && diskon >= 1)
                                                ? diskon / hargaKotor * 100
                                                : 0;
                                        return Column(
                                            mainAxisAlignment:
                                                MainAxisAlignment.center,
                                            children: [
                                              (persentaseDiskon > 0)
                                                  ? SingleChildScrollView(
                                                      scrollDirection:
                                                          Axis.horizontal,
                                                      child: Row(children: [
                                                        Text(
                                                            Currency
                                                                .formatRupiah(
                                                                    hargaKotor,
                                                                    0),
                                                            style: TextStyle(
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    3)),
                                                        Container(
                                                            margin:
                                                                EdgeInsets.only(
                                                                    left: 5),
                                                            child: Text(
                                                                '-' +
                                                                    Currency.formatSatuan(
                                                                        persentaseDiskon
                                                                            .toInt(),
                                                                        satuan:
                                                                            '%'),
                                                                style: TextStyle(
                                                                    fontSize:
                                                                        SizeConfig.horizontalBlock *
                                                                            2.75)),
                                                            padding: EdgeInsets.symmetric(
                                                                horizontal: 10,
                                                                vertical: 2.5),
                                                            decoration: BoxDecoration(
                                                                color: Colors
                                                                    .amber,
                                                                borderRadius:
                                                                    BorderRadius.circular(50)))
                                                      ]),
                                                    )
                                                  : SizedBox(),
                                              Container(
                                                  margin:
                                                      EdgeInsets.only(top: 5),
                                                  child: Text(
                                                      Currency.formatRupiah(
                                                          hargaBersih, 0),
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25,
                                                          color: CustomColors
                                                              .green,
                                                          fontWeight: FontWeight
                                                              .w700))),
                                              SizedBox(
                                                  height: (detailPaket != false)
                                                      ? 10
                                                      : 0),
                                              (detailPaket != false)
                                                  ? Padding(
                                                      padding:
                                                          EdgeInsets.symmetric(
                                                              horizontal: 10),
                                                      child: CustomButton.button(
                                                          context, 'Beli',
                                                          () async {
                                                        bool isADJDSet =
                                                            await LocalStorage
                                                                .isAlamatDeliveryDanJadwalDeliverySet();
                                                        if (isADJDSet) {
                                                          ProdukModel
                                                              .showSatuanProduk(
                                                                  context,
                                                                  dataProduk);
                                                        } else {
                                                          bool?
                                                              lakukanDeteksiADJD =
                                                              await ProdukModel
                                                                  .adjdNotSetAlert(
                                                                      context);
                                                          if (lakukanDeteksiADJD !=
                                                                  null &&
                                                              lakukanDeteksiADJD) {
                                                            deteksiADJD();
                                                          }
                                                        }
                                                      },
                                                          color: CustomButton
                                                              .themeColor,
                                                          size: CustomButton
                                                              .smSize,
                                                          isPill: true,
                                                          isBlock: true),
                                                    )
                                                  : SizedBox()
                                            ]);
                                      }
                                    } else {
                                      return Center(
                                          child: SizedBox(
                                              width: 15,
                                              height: 15,
                                              child: CircularProgressIndicator(
                                                  strokeWidth: 1.5)));
                                    }
                                  }),
                            )
                          ]),
                    ),
                  ),
                ],
              )),
        ));
  }
}
