import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/views/penjual/TambahProduk.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

import 'package:laukpauk/views/pembeli/DetailProduk.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/models/ProdukModel.dart';

class ProdukPenjualCard extends StatefulWidget {
  final Map<String, dynamic> dataProduk;
  final void Function() deteksiADJD;
  final void Function() reload;
  ProdukPenjualCard(this.dataProduk,
      {required this.deteksiADJD, required this.reload});

  _ProdukPenjualCardState createState() => _ProdukPenjualCardState();
}

class _ProdukPenjualCardState extends State<ProdukPenjualCard> {
  int? currentUserIDState;

  Future<dynamic> getLowestPrice() async {
    String idProduk = widget.dataProduk['id'];

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

  void initialization() async {
    String currentUserIDString = await LocalStorage.getCurrentUserID();
    int currentUserID = int.parse(currentUserIDString);
    setState(() {
      currentUserIDState = currentUserID;
    });
  }

  void initState() {
    super.initState();
    this.initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> dataProduk = widget.dataProduk;

    String namaKategoriProduk =
        (widget.dataProduk['namaKategoriProduk'] != null)
            ? widget.dataProduk['namaKategoriProduk']
            : 'Kategori Tidak Diketahui';

    int idPenjual = int.parse(widget.dataProduk['idPenjual']);
    bool produkSaya = (currentUserIDState == idPenjual);
    String fotoProduk = dataProduk['foto'];
    bool isDefaultImage = (fotoProduk == DefaultImage.defaultProduk);

    return Container(
        padding: EdgeInsets.only(top: 5),
        child: InkWell(
          onTap: () {
            Navigator.push(context, MaterialPageRoute(builder: (_) {
              return DetailProduk(widget.dataProduk['id']);
            }));
          },
          child: Card(
              margin: EdgeInsets.only(bottom: 10, left: 2.5, right: 2.5),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              child: Container(
                decoration:
                    BoxDecoration(borderRadius: BorderRadius.circular(10)),
                child: Center(
                  child: Column(
                    children: [
                      Expanded(
                          child: LayoutBuilder(builder: (context, constraints) {
                        return Container(
                            constraints: BoxConstraints(
                                minHeight: constraints.maxHeight,
                                minWidth: constraints.maxWidth),
                            margin: EdgeInsets.only(bottom: 10),
                            decoration: BoxDecoration(
                                borderRadius: BorderRadius.only(
                                    topLeft: Radius.circular(10.0),
                                    topRight: Radius.circular(10.0)),
                                image: DecorationImage(
                                    image: NetworkImage(ServerPath.apiBaseURL +
                                        '/' +
                                        ServerPath.uploadGambarProduk +
                                        '/compress/' +
                                        fotoProduk),
                                    fit: (isDefaultImage)
                                        ? BoxFit.contain
                                        : BoxFit.fill)));
                      })),
                      Expanded(
                        child: SingleChildScrollView(
                          child: Padding(
                            padding: EdgeInsets.symmetric(horizontal: 5.0),
                            child: Column(children: [
                              Text(widget.dataProduk['nama'],
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5,
                                      fontWeight: FontWeight.w700),
                                  textAlign: TextAlign.center),
                              SizedBox(height: 2),
                              Text(namaKategoriProduk,
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                      fontStyle: (widget.dataProduk[
                                                  'namaKategoriProduk'] ==
                                              null)
                                          ? FontStyle.italic
                                          : null,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3)),
                              (!produkSaya) ? SizedBox(height: 5) : SizedBox(),
                              (!produkSaya)
                                  ? FutureBuilder(
                                      future: getLowestPrice(),
                                      builder: (context, snapshot) {
                                        if (snapshot.hasData) {
                                          dynamic detailHarga = snapshot.data;
                                          if (detailHarga == false) {
                                            return Text(
                                                'Produk ini tidak memiliki daftar harga',
                                                textAlign: TextAlign.center,
                                                style: TextStyle(
                                                    color:
                                                        CustomColors.textMuted,
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        3));
                                          } else {
                                            int hargaBersih = int.parse(
                                                detailHarga['hargaBersih']);
                                            int hargaKotor =
                                                int.parse(detailHarga['harga']);
                                            int diskon = int.parse(
                                                detailHarga['diskon']);
                                            double persentaseDiskon =
                                                (diskon >= 1 && hargaKotor >= 1)
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
                                                          child: Row(
                                                              mainAxisAlignment:
                                                                  MainAxisAlignment
                                                                      .spaceEvenly,
                                                              children: [
                                                                Text(
                                                                    Currency.formatRupiah(
                                                                        hargaKotor,
                                                                        0),
                                                                    style: TextStyle(
                                                                        fontSize:
                                                                            SizeConfig.horizontalBlock *
                                                                                2.75)),
                                                                Container(
                                                                    margin: EdgeInsets
                                                                        .only(
                                                                            left:
                                                                                5),
                                                                    child: Text(
                                                                        '-' +
                                                                            Currency.formatSatuan(persentaseDiskon.toInt(),
                                                                                satuan:
                                                                                    '%'),
                                                                        style: TextStyle(
                                                                            fontSize: SizeConfig.horizontalBlock *
                                                                                2.75)),
                                                                    padding: EdgeInsets.symmetric(
                                                                        horizontal:
                                                                            10,
                                                                        vertical:
                                                                            2.5),
                                                                    decoration: BoxDecoration(
                                                                        color: Colors
                                                                            .amber,
                                                                        borderRadius:
                                                                            BorderRadius.circular(50)))
                                                              ]),
                                                        )
                                                      : SizedBox(),
                                                  Container(
                                                      margin: EdgeInsets.only(
                                                          top: 2.5),
                                                      child: Text(
                                                          Currency.formatRupiah(
                                                              hargaBersih, 0),
                                                          style: TextStyle(
                                                              fontSize: SizeConfig
                                                                      .horizontalBlock *
                                                                  3.25,
                                                              color:
                                                                  CustomColors
                                                                      .green,
                                                              fontWeight:
                                                                  FontWeight
                                                                      .w700))),
                                                  (!produkSaya)
                                                      ? SizedBox(height: 10)
                                                      : SizedBox(),
                                                  (!produkSaya)
                                                      ? Padding(
                                                          padding: EdgeInsets
                                                              .symmetric(
                                                                  horizontal:
                                                                      10),
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
                                                                      widget
                                                                          .dataProduk);
                                                            } else {
                                                              bool?
                                                                  lakukanDeteksiADJD =
                                                                  await ProdukModel
                                                                      .adjdNotSetAlert(
                                                                          context);
                                                              if (lakukanDeteksiADJD !=
                                                                      null &&
                                                                  lakukanDeteksiADJD) {
                                                                widget
                                                                    .deteksiADJD();
                                                              }
                                                            }
                                                          },
                                                              isPill: true,
                                                              color: CustomButton
                                                                  .themeColor,
                                                              isBlock: true,
                                                              size: CustomButton
                                                                  .smSize),
                                                        )
                                                      : SizedBox()
                                                ]);
                                          }
                                        } else {
                                          return Center(
                                              child: SizedBox(
                                                  width: 15,
                                                  height: 15,
                                                  child:
                                                      CircularProgressIndicator(
                                                          strokeWidth: 1.5)));
                                        }
                                      })
                                  : SizedBox(),
                              (produkSaya)
                                  ? Padding(
                                      padding: EdgeInsets.only(
                                          top: 10.0, left: 10.0, right: 10.0),
                                      child: CustomButton.button(
                                          context, 'Edit', () async {
                                        bool? statusEditProduk =
                                            await Navigator.push(
                                                context,
                                                MaterialPageRoute(
                                                    builder: (_) =>
                                                        TambahProduk(
                                                            widget.dataProduk[
                                                                'id'],
                                                            'satuan')));
                                        if (statusEditProduk != null) {
                                          String text = (statusEditProduk)
                                              ? 'Berhasil mengubah data produk'
                                              : 'Gagal mengubah data produk !';
                                          SnackBar snackBar = SnackBar(
                                              content: Text(text),
                                              duration: Duration(seconds: 2));
                                          // Scaffold.of(context)
                                          //     .showSnackBar(snackBar);

                                          ScaffoldMessenger.of(context)
                                              .showSnackBar(snackBar);
                                          widget.reload();
                                        }
                                      },
                                          isPill: true,
                                          color: CustomButton.warningColor,
                                          isBlock: true,
                                          size: CustomButton.smSize),
                                    )
                                  : SizedBox()
                            ]),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              )),
        ));
  }
}
