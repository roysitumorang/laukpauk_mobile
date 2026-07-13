import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/views/penjual/components/TokoSaya/PenjualanDanTransferDana.dart';
import 'package:laukpauk/views/penjual/components/TokoSaya/TransaksiPenjual.dart';
import 'package:laukpauk/views/penjual/components/TokoSaya/ProdukPenjual.dart';
import 'package:laukpauk/views/penjual/components/TokoSaya/TagihanToko.dart';
import 'package:laukpauk/views/penjual/components/TokoSaya/JamDelivery.dart';
import 'package:laukpauk/views/penjual/UbahNamaToko.dart';
import 'package:laukpauk/views/pembeli/components/ProfilSaya/RMP.dart';

import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:laukpauk/views/penjual/UpdateHarga.dart';
import 'package:laukpauk/views/Penjual.dart';

import 'package:laukpauk/models/TokoSayaModel.dart';

import 'package:laukpauk/models/AuthModel.dart';

class TokoSaya extends StatefulWidget {
  _TokoSayaState createState() => _TokoSayaState();
}

class _TokoSayaState extends State<TokoSaya> {
  int reload = 0;

  Future<dynamic> getDetailToko() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    int idPenjualInt = await LocalStorage.getIdPenjualFromActiveUser();
    String idPenjual = idPenjualInt.toString();

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Penjual/getDetailPenjual/$idPenjual?' +
        _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      dynamic detailPenjual = dataObject['detailPenjual'];

      return detailPenjual;
    }
    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return FutureBuilder<bool>(
        future: AuthModel.isLogin(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              bool isLogin = snapshot.data!;
              if (isLogin) {
                return Scaffold(
                    body: FutureBuilder(
                        builder: (context, snapshot) {
                          if (snapshot.hasData) {
                            dynamic dataPenjual = snapshot.data;

                            if (dataPenjual != false) {
                              String statusApprove = dataPenjual['status'];
                              String namaPasar =
                                  (dataPenjual['pasarUmum'] == '0')
                                      ? 'Lokasi Tersendiri'
                                      : dataPenjual['namaPasar'];
                              String alamatToko = dataPenjual['alamatToko'];

                              if (statusApprove.toLowerCase() == 'approved') {
                                return Column(
                                    mainAxisAlignment: MainAxisAlignment.start,
                                    children: [
                                      Header(),
                                      Expanded(
                                          child: RefreshIndicator(
                                        onRefresh: () async {
                                          setState(() {});
                                        },
                                        child: ListView(
                                            padding: EdgeInsets.zero,
                                            children: [
                                              Padding(
                                                  padding: EdgeInsets.only(
                                                      top: 5,
                                                      right: 5,
                                                      left: 5),
                                                  child: Card(
                                                      elevation: 5,
                                                      shape:
                                                          RoundedRectangleBorder(
                                                              borderRadius:
                                                                  BorderRadius
                                                                      .circular(
                                                                          10)),
                                                      child: Padding(
                                                        padding:
                                                            EdgeInsets.only(
                                                                top: 10,
                                                                bottom: 20,
                                                                left: 15,
                                                                right: 15),
                                                        child: Column(
                                                            children: [
                                                              Row(
                                                                  mainAxisAlignment:
                                                                      MainAxisAlignment
                                                                          .start,
                                                                  crossAxisAlignment:
                                                                      CrossAxisAlignment
                                                                          .start,
                                                                  children: [
                                                                    Container(
                                                                        width: SizeConfig.horizontalBlock *
                                                                            17.5,
                                                                        height: SizeConfig.horizontalBlock *
                                                                            17.5,
                                                                        decoration: BoxDecoration(
                                                                            image:
                                                                                DecorationImage(fit: BoxFit.cover, image: NetworkImage(ServerPath.baseURL + '/' + ServerPath.uploadGambarBannerPenjual + '/compress/' + dataPenjual['banner'])),
                                                                            borderRadius: BorderRadius.circular(50))),
                                                                    SizedBox(
                                                                        width:
                                                                            15),
                                                                    Expanded(
                                                                      child: Column(
                                                                          crossAxisAlignment:
                                                                              CrossAxisAlignment.start,
                                                                          children: [
                                                                            InkWell(
                                                                              onTap: () {
                                                                                Navigator.push(context, MaterialPageRoute(builder: (_) => Penjual(id: dataPenjual['id'])));
                                                                              },
                                                                              child: Text(dataPenjual['namaToko'], style: TextStyle(color: CustomColors.green, fontSize: SizeConfig.horizontalBlock * 4.25, fontWeight: FontWeight.w700)),
                                                                            ),
                                                                            SizedBox(height: 3),
                                                                            Text((alamatToko.length >= 1) ? alamatToko : 'Alamat Toko Belum Ditentukan',
                                                                                style: TextStyle(fontStyle: (alamatToko.length >= 1) ? null : FontStyle.italic, fontSize: SizeConfig.horizontalBlock * 3, color: CustomColors.textMuted)),
                                                                            SizedBox(height: 5),
                                                                            Row(
                                                                              mainAxisAlignment: MainAxisAlignment.start,
                                                                              children: [
                                                                                (dataPenjual['namaPasar'] != null)
                                                                                    ? Expanded(
                                                                                        child: Text('Berjualan di ' + namaPasar, style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3, color: CustomColors.textMuted)),
                                                                                      )
                                                                                    : SizedBox(),
                                                                                SizedBox(width: 5),
                                                                                InkWell(
                                                                                  onTap: () async {
                                                                                    bool? statusUpdateLokasiBerjualan = await TokoSayaModel.showModalUbahLokasiBerjualan(context, dataPenjual);
                                                                                    if (statusUpdateLokasiBerjualan != null) {
                                                                                      String message = (statusUpdateLokasiBerjualan) ? 'Berhasil mengubah lokasi berjualan.' : 'Gagal mengubah lokasi berjualan.';
                                                                                      SnackBar snackBarMessage = SnackBar(duration: Duration(seconds: 1), content: Text(message, style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)));

                                                                                      ScaffoldMessenger.of(context).showSnackBar(snackBarMessage);
                                                                                      if (statusUpdateLokasiBerjualan) {
                                                                                        setState(() {
                                                                                          reload++;
                                                                                        });
                                                                                      }
                                                                                    }
                                                                                  },
                                                                                  child: Icon(Icons.pin_drop, size: SizeConfig.horizontalBlock * 4.5, color: CustomColors.textPrimary),
                                                                                )
                                                                              ],
                                                                            ),
                                                                            SizedBox(height: 10),
                                                                            InkWell(
                                                                              onTap: () {
                                                                                Navigator.push(context, MaterialPageRoute(builder: (_) => Penjual(id: dataPenjual['id'])));
                                                                              },
                                                                              child: Text('Lihat Toko Saya', style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3, color: CustomColors.textPrimary)),
                                                                            )
                                                                          ]),
                                                                    ),
                                                                    InkWell(
                                                                      onTap:
                                                                          () {
                                                                        Navigator.push(
                                                                            context,
                                                                            MaterialPageRoute(builder: (_) => UpdateHarga()));
                                                                      },
                                                                      child: Text(
                                                                          'Update Harga',
                                                                          style: TextStyle(
                                                                              fontSize: SizeConfig.horizontalBlock * 3.25,
                                                                              color: CustomColors.textPrimary)),
                                                                    )
                                                                  ]),
                                                              SizedBox(
                                                                  height: 20),
                                                              PenjualanDanTransferDana()
                                                            ]),
                                                      ))),
                                              Padding(
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 5),
                                                  child: TransaksiPenjual()),
                                              Padding(
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 5),
                                                  child: TagihanToko()),
                                              Padding(
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 5),
                                                  child: ProdukPenjual(
                                                      (statusSaveProduk) {
                                                    if (statusSaveProduk !=
                                                        null) {
                                                      String message =
                                                          (statusSaveProduk)
                                                              ? 'Produk baru berhasil ditambahkan ke toko kamu.'
                                                              : 'Produk baru gagal disimpan !';
                                                      SnackBar
                                                          snackBarTambahProduk =
                                                          SnackBar(
                                                              duration:
                                                                  Duration(
                                                                      seconds:
                                                                          1),
                                                              content: Text(
                                                                  message,
                                                                  style: TextStyle(
                                                                      fontSize:
                                                                          SizeConfig.horizontalBlock *
                                                                              3.25)));

                                                      ScaffoldMessenger.of(
                                                              context)
                                                          .showSnackBar(
                                                              snackBarTambahProduk);

                                                      if (statusSaveProduk) {
                                                        setState(() {
                                                          reload++;
                                                        });
                                                      }
                                                    }
                                                  })),
                                              Padding(
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 5),
                                                  child: JamDelivery()),
                                              Padding(
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 5),
                                                  child: UbahNamaToko(
                                                      (statusUbahNamaToko) {
                                                    if (statusUbahNamaToko !=
                                                        null) {
                                                      String message =
                                                          (statusUbahNamaToko)
                                                              ? 'Nama Toko berhasil diubah'
                                                              : 'Nama Toko gagal diubah !';
                                                      SnackBar
                                                          snackBarUbahNamaToko =
                                                          SnackBar(
                                                              duration:
                                                                  Duration(
                                                                      seconds:
                                                                          1),
                                                              content: Text(
                                                                  message,
                                                                  style: TextStyle(
                                                                      fontSize:
                                                                          SizeConfig.horizontalBlock *
                                                                              3.25)));
                                                      // Scaffold.of(context)
                                                      //     .showSnackBar(
                                                      //         snackBarUbahNamaToko);

                                                      ScaffoldMessenger.of(
                                                              context)
                                                          .showSnackBar(
                                                              snackBarUbahNamaToko);

                                                      if (statusUbahNamaToko) {
                                                        setState(() {
                                                          reload++;
                                                        });
                                                      }
                                                    }
                                                  }))
                                            ]),
                                      ))
                                    ]);
                              } else {
                                return RMP(dataPenjual);
                              }
                            } else {
                              return Container(
                                width: MediaQuery.of(context).size.width,
                                height: MediaQuery.of(context).size.height,
                                child: Column(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      Image.asset(
                                          ClientPath.assetsImg + '/empty.png',
                                          width:
                                              SizeConfig.horizontalBlock * 25),
                                      SizedBox(height: 10),
                                      Text('Data Akun Anda tidak Ditemukan',
                                          style: TextStyle(
                                              color: CustomColors.textDanger,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      4))
                                    ]),
                              );
                            }
                          } else {
                            return Loading();
                          }
                        },
                        future: getDetailToko()),
                    bottomNavigationBar: Footer());
              } else {
                return Login();
              }
            }
          }
          return Scaffold(body: Loading());
        });
  }
}
