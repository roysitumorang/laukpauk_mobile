import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/penjual/components/UpdateHarga/SatuanProduk.dart';
import 'package:laukpauk/models/ProdukPenjualModel.dart';

class DetailProdukPenjual extends StatefulWidget {
  final String id, jenisProduk;
  DetailProdukPenjual(this.id, this.jenisProduk);

  DetailProdukPenjualState createState() => DetailProdukPenjualState();
}

class DetailProdukPenjualState extends State<DetailProdukPenjual> {
  TextEditingController deskripsiProdukController = TextEditingController();

  bool isUpdateDeskripsiProduk = false;

  Future<dynamic> getDetailProduk() async {
    String idProduk = widget.id;
    String jenisProduk = widget.jenisProduk;

    if (jenisProduk.toLowerCase() == 'satuan') {
      String _params =
          await HTTPRequest.getQueryString(params: 'idProduk=$idProduk');

      Uri _url = Uri.parse(
          ServerPath.apiBaseURL + '/Produk/getHargaProduk?' + _params);
      Response httpResponse = await get(_url);

      if (httpResponse.statusCode == 200) {
        dynamic dataObject = jsonDecode(httpResponse.body);
        return dataObject['detailProduk'];
      }
    } else {
      String _params =
          await HTTPRequest.getQueryString(params: 'idPaket=$idProduk');

      Uri _url =
          Uri.parse(ServerPath.apiBaseURL + '/Produk/detailPaket?' + _params);
      Response httpResponse = await get(_url);
      if (httpResponse.statusCode == 200) {
        dynamic dataObject = jsonDecode(httpResponse.body);
        return dataObject['detailPaket'];
      }
    }

    return false;
  }

  Future<List> getKategoriProduk() async {
    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/KategoriProduk/getKategoriProduk');
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List kategoriProduk = dataObject['kategoriProduk'];

      return kategoriProduk;
    }
    return [];
  }

  void dispose() {
    super.dispose();
    deskripsiProdukController.dispose();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String jenisProduk = widget.jenisProduk;
    String uploadPath = (jenisProduk.toLowerCase() == 'satuan')
        ? ServerPath.uploadGambarProduk
        : ServerPath.uploadGambarPaket;

    return Scaffold(
        backgroundColor: CustomColors.aliceBlue,
        body: Column(mainAxisAlignment: MainAxisAlignment.start, children: [
          Header(),
          Expanded(
              child: FutureBuilder(
                  future: getDetailProduk(),
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      dynamic detailProduk = snapshot.data;
                      if (detailProduk != false) {
                        bool isSuperior = detailProduk['superior'] == '1';
                        String namaKategoriProduk =
                            (detailProduk['namaKategoriProduk'] != null)
                                ? detailProduk['namaKategoriProduk']
                                : 'Kategori Produk Tidak Diketahui';

                        return RefreshIndicator(
                          onRefresh: () async {
                            setState(() {});
                          },
                          child: ListView(padding: EdgeInsets.zero, children: [
                            Container(
                              width: SizeConfig.horizontalBlock * 100,
                              height: SizeConfig.horizontalBlock * 60,
                              decoration: BoxDecoration(
                                  image: DecorationImage(
                                      fit: BoxFit.cover,
                                      image: NetworkImage(ServerPath.baseURL +
                                          '/' +
                                          uploadPath +
                                          '/' +
                                          detailProduk['foto']))),
                            ),
                            Padding(
                                padding: EdgeInsets.symmetric(
                                    horizontal: 2.5, vertical: 5),
                                child: Card(
                                  elevation: 5,
                                  shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(10)),
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
                                                MainAxisAlignment.spaceBetween,
                                            children: [
                                              Expanded(
                                                child: Column(
                                                  crossAxisAlignment:
                                                      CrossAxisAlignment.start,
                                                  children: [
                                                    Text(
                                                        detailProduk['nama']
                                                            .toUpperCase(),
                                                        style: TextStyle(
                                                            color: CustomColors
                                                                .green,
                                                            fontSize: SizeConfig
                                                                    .horizontalBlock *
                                                                4.5,
                                                            fontWeight:
                                                                FontWeight
                                                                    .w700)),
                                                    (jenisProduk.toLowerCase() ==
                                                            'satuan')
                                                        ? SizedBox(height: 5)
                                                        : SizedBox(),
                                                    (jenisProduk.toLowerCase() ==
                                                            'satuan')
                                                        ? Text(
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
                                                                    3.5,
                                                                color: CustomColors
                                                                    .textMuted))
                                                        : SizedBox()
                                                  ],
                                                ),
                                              ),
                                              SizedBox(width: 10),
                                              Column(
                                                crossAxisAlignment:
                                                    CrossAxisAlignment.end,
                                                children: [
                                                  Switch(
                                                      activeColor:
                                                          CustomColors.green,
                                                      value: isSuperior,
                                                      onChanged: (value) async {
                                                        String idProduk =
                                                            widget.id;
                                                        bool
                                                            statusToggleProdukPalingLaris =
                                                            await ProdukPenjualModel
                                                                .toggleProdukPalingLaris(
                                                                    idProduk,
                                                                    jenisProduk);
                                                        if (statusToggleProdukPalingLaris) {
                                                          setState(() {
                                                            isSuperior = value;
                                                          });
                                                        }
                                                      }),
                                                  Text('Produk Paling Laris',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3))
                                                ],
                                              )
                                            ]),
                                        (jenisProduk.toLowerCase() == 'satuan')
                                            ? FutureBuilder<List>(
                                                builder: (context, snapshot) {
                                                  if (snapshot.hasData) {
                                                    if (snapshot.data != null) {
                                                      List kategoriProduk =
                                                          snapshot.data!;
                                                      if (kategoriProduk
                                                              .length >=
                                                          1) {
                                                        String
                                                            kategoriProdukSelected =
                                                            (detailProduk[
                                                                        'idKategoriProduk'] !=
                                                                    null)
                                                                ? detailProduk[
                                                                        'idKategoriProduk']
                                                                    .toString()
                                                                : '0';
                                                        TextStyle
                                                            dropdownButtonsText =
                                                            TextStyle(
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    3);

                                                        List<
                                                            DropdownMenuItem<
                                                                String>> dropdownButtons = kategoriProduk
                                                            .map((kategori) => DropdownMenuItem(
                                                                value: kategori[
                                                                        'id']
                                                                    .toString(),
                                                                child: Text(
                                                                    kategori[
                                                                        'nama'],
                                                                    style:
                                                                        dropdownButtonsText)))
                                                            .toList();
                                                        return Padding(
                                                          padding: EdgeInsets
                                                              .symmetric(
                                                                  vertical: 10),
                                                          child: Column(
                                                            crossAxisAlignment:
                                                                CrossAxisAlignment
                                                                    .start,
                                                            children: [
                                                              Text(
                                                                  'Kategori Produk',
                                                                  style: TextStyle(
                                                                      fontSize:
                                                                          SizeConfig.horizontalBlock *
                                                                              4,
                                                                      fontWeight:
                                                                          FontWeight
                                                                              .w700)),
                                                              SizedBox(
                                                                  height: 5),
                                                              DropdownButton<
                                                                      String>(
                                                                  value:
                                                                      kategoriProdukSelected,
                                                                  underline:
                                                                      DropdownButtonHideUnderline(
                                                                          child:
                                                                              SizedBox()),
                                                                  isDense: true,
                                                                  isExpanded:
                                                                      true,
                                                                  items: [
                                                                    DropdownMenuItem(
                                                                        value:
                                                                            '0',
                                                                        child: Text(
                                                                            'Pilih Kategori Produk',
                                                                            style:
                                                                                dropdownButtonsText)),
                                                                    ...dropdownButtons
                                                                  ],
                                                                  onChanged:
                                                                      (selected) async {
                                                                    if (selected !=
                                                                        null) {
                                                                      String
                                                                          idProduk =
                                                                          detailProduk['id']
                                                                              .toString();

                                                                      bool statusUbahKategoriProduk = await ProdukPenjualModel.ubahKategoriProduk(
                                                                          idProduk,
                                                                          selected);
                                                                      if (statusUbahKategoriProduk) {
                                                                        setState(
                                                                            () {
                                                                          kategoriProdukSelected =
                                                                              selected;
                                                                        });
                                                                      }
                                                                    }
                                                                  }),
                                                            ],
                                                          ),
                                                        );
                                                      } else {
                                                        return Center(
                                                          child: Padding(
                                                            padding: EdgeInsets
                                                                .symmetric(
                                                                    vertical:
                                                                        20),
                                                            child: Column(
                                                                children: [
                                                                  Image.asset(
                                                                      ClientPath
                                                                              .assetsImg +
                                                                          '/empty.png',
                                                                      width: SizeConfig
                                                                              .horizontalBlock *
                                                                          27.5),
                                                                  SizedBox(
                                                                      height:
                                                                          5),
                                                                  Text(
                                                                      'Tidak ada kategori produk yang bisa dipilih.',
                                                                      textAlign:
                                                                          TextAlign
                                                                              .center,
                                                                      style: TextStyle(
                                                                          fontSize:
                                                                              SizeConfig.horizontalBlock * 3.5))
                                                                ]),
                                                          ),
                                                        );
                                                      }
                                                    }
                                                  }
                                                  return Padding(
                                                    padding:
                                                        EdgeInsets.symmetric(
                                                            vertical: 15),
                                                    child: Center(
                                                        child: SizedBox(
                                                            width: 15,
                                                            height: 15,
                                                            child:
                                                                CircularProgressIndicator(
                                                                    strokeWidth:
                                                                        1.5))),
                                                  );
                                                },
                                                future: getKategoriProduk())
                                            : SizedBox(),
                                        SizedBox(
                                            height:
                                                (jenisProduk.toLowerCase() ==
                                                        'satuan')
                                                    ? 10
                                                    : 0),
                                        (jenisProduk.toLowerCase() == 'satuan')
                                            ? Container(
                                                padding: EdgeInsets.symmetric(
                                                    vertical: 15),
                                                child: SatuanProduk(
                                                    detailProduk['id'],
                                                    showNomorUrut: true,
                                                    showOptions: true,
                                                    updateAble: false),
                                                decoration: BoxDecoration(
                                                    border: Border(
                                                        top: BorderSide(
                                                            width: 0.5,
                                                            color: CustomColors
                                                                .textMuted),
                                                        bottom: BorderSide(
                                                            width: 0.5,
                                                            color: CustomColors
                                                                .textMuted))))
                                            : SizedBox(),
                                        SizedBox(height: 10),
                                        Column(
                                            mainAxisAlignment:
                                                MainAxisAlignment.start,
                                            crossAxisAlignment:
                                                CrossAxisAlignment.start,
                                            children: [
                                              Row(
                                                mainAxisAlignment:
                                                    MainAxisAlignment
                                                        .spaceBetween,
                                                children: [
                                                  Text('Deskripsi Produk',
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              4,
                                                          fontWeight:
                                                              FontWeight.w700)),
                                                  InkWell(
                                                      onTap: () {
                                                        setState(() {
                                                          isUpdateDeskripsiProduk =
                                                              !isUpdateDeskripsiProduk;
                                                        });
                                                      },
                                                      child: Text(
                                                          (!isUpdateDeskripsiProduk)
                                                              ? 'Ubah'
                                                              : 'Batal',
                                                          style: TextStyle(
                                                              fontSize:
                                                                  SizeConfig
                                                                          .horizontalBlock *
                                                                      3.25,
                                                              color: (!isUpdateDeskripsiProduk)
                                                                  ? CustomColors
                                                                      .textPrimary
                                                                  : CustomColors
                                                                      .textDanger)))
                                                ],
                                              ),
                                              SizedBox(height: 5),
                                              (!isUpdateDeskripsiProduk)
                                                  ? Text(
                                                      detailProduk[
                                                          'keterangan'],
                                                      style: TextStyle(
                                                          color: CustomColors
                                                              .textMuted,
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.5))
                                                  : Column(
                                                      mainAxisAlignment:
                                                          MainAxisAlignment
                                                              .start,
                                                      children: [
                                                          Container(
                                                            decoration: BoxDecoration(
                                                                border: Border.all(
                                                                    color:
                                                                        CustomColors
                                                                            .grey,
                                                                    width:
                                                                        0.25),
                                                                borderRadius:
                                                                    BorderRadius
                                                                        .circular(
                                                                            10),
                                                                color: CustomColors
                                                                    .lightGrey),
                                                            child: Padding(
                                                                padding:
                                                                    EdgeInsets.only(
                                                                        left:
                                                                            15,
                                                                        right:
                                                                            15),
                                                                child: TextField(
                                                                    maxLines:
                                                                        null,
                                                                    minLines: 4,
                                                                    controller:
                                                                        deskripsiProdukController,
                                                                    style: TextStyle(
                                                                        fontSize:
                                                                            SizeConfig.horizontalBlock *
                                                                                3.25),
                                                                    keyboardType:
                                                                        TextInputType
                                                                            .text,
                                                                    decoration: InputDecoration(
                                                                        isDense:
                                                                            true,
                                                                        contentPadding: EdgeInsets.only(
                                                                            top: SizeConfig.verticalBlock *
                                                                                2,
                                                                            bottom: SizeConfig.verticalBlock *
                                                                                2),
                                                                        hintText:
                                                                            'Deskripsi Produk',
                                                                        border: InputBorder.none))),
                                                          ),
                                                          TextButton(
                                                              onPressed:
                                                                  () async {
                                                                String
                                                                    idProduk =
                                                                    widget.id;
                                                                bool
                                                                    statusUbahDeskripsiProduk =
                                                                    await ProdukPenjualModel.ubahDeskripsiProduk(
                                                                        idProduk,
                                                                        jenisProduk,
                                                                        deskripsiProdukController
                                                                            .text);
                                                                if (statusUbahDeskripsiProduk) {
                                                                  setState(() {
                                                                    isUpdateDeskripsiProduk =
                                                                        false;
                                                                  });
                                                                }
                                                              },
                                                              child: Text(
                                                                  'Simpan Perubahan Deskripsi Produk',
                                                                  style: TextStyle(
                                                                      color: CustomColors
                                                                          .green,
                                                                      fontSize:
                                                                          SizeConfig.horizontalBlock *
                                                                              3)))
                                                        ])
                                            ])
                                      ],
                                    ),
                                  ),
                                ))
                          ]),
                        );
                      } else {
                        return Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Image.asset(
                                ClientPath.assetsImg + '/empty.png',
                                width: 200,
                              ),
                              SizedBox(height: 20),
                              Text('Detail Produk Tidak Ditemukan',
                                  style: TextStyle(
                                      color: CustomColors.textDanger,
                                      fontSize: 16,
                                      fontWeight: FontWeight.w600))
                            ]);
                      }
                    } else {
                      return Center(
                          child: SizedBox(
                              width: 15,
                              height: 15,
                              child:
                                  CircularProgressIndicator(strokeWidth: 1.5)));
                    }
                  })),
        ]),
        bottomNavigationBar: Footer());
  }
}
