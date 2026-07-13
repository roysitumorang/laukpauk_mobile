import 'dart:convert';

import 'package:flutter/material.dart';

import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/views/Penjual.dart';
import 'package:laukpauk/views/penjual/ListProdukPenjual.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/penjual/TambahProduk.dart';

class ProdukPenjual extends StatefulWidget {
  final Function(bool? statusSaveProduk) saveProdukCallback;
  ProdukPenjual(this.saveProdukCallback);
  _ProdukPenjualState createState() => _ProdukPenjualState();
}

class _ProdukPenjualState extends State<ProdukPenjual> {
  Future<dynamic> getJumlahProdukPenjual() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Penjual/getJumlahProdukPenjual?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      Map jumlahProduk = dataObject['jumlahProduk'];

      return jumlahProduk;
    }

    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Card(
        elevation: 5,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 15, vertical: 10),
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Produk Saya',
                style: TextStyle(
                    fontSize: SizeConfig.horizontalBlock * 4,
                    fontWeight: FontWeight.w700)),
            SizedBox(height: 15),
            FutureBuilder(
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    dynamic dataProduk = snapshot.data;
                    if (dataProduk != false) {
                      return Row(
                          mainAxisAlignment: MainAxisAlignment.spaceAround,
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Column(children: [
                              Text('Produk Satuan',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5)),
                              SizedBox(height: 5),
                              InkWell(
                                onTap: () async {
                                  String idUser =
                                      await LocalStorage.getApplicationID();

                                  Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => Penjual(
                                                id: idUser,
                                                jenisProdukSelected: 'satuan',
                                              )));
                                },
                                child: Text(
                                    Currency.formatSatuan(dataProduk['produk'],
                                        satuan: ''),
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 7,
                                        fontWeight: FontWeight.w700,
                                        color: CustomColors.green)),
                              ),
                              Text('Produk',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 3))
                            ]),
                            Column(children: [
                              Text('Produk Paketan',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5)),
                              SizedBox(height: 5),
                              InkWell(
                                onTap: () async {
                                  String idUser =
                                      await LocalStorage.getCurrentUserID();
                                  Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => Penjual(
                                                id: idUser,
                                                jenisProdukSelected: 'paket',
                                              )));
                                },
                                child: Text(
                                    Currency.formatSatuan(dataProduk['paket'],
                                        satuan: ''),
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 7,
                                        fontWeight: FontWeight.w700,
                                        color: CustomColors.green)),
                              ),
                              Text('Produk',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 3))
                            ])
                          ]);
                    } else {
                      return Text('Gagal mengambil data produk');
                    }
                  } else {
                    return Container(
                        padding: EdgeInsets.symmetric(vertical: 15),
                        child: Center(
                            child: SizedBox(
                                width: 15,
                                height: 15,
                                child: CircularProgressIndicator(
                                    strokeWidth: 1.5))));
                  }
                },
                future: getJumlahProdukPenjual()),
            SizedBox(height: 10),
            Center(
                child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                TextButton(
                    onPressed: () {
                      Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) => ListProdukPenjual()));
                    },
                    child: Text('Lihat Produk Saya',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.5))),
                TextButton(
                    onPressed: () async {
                      bool? statusSaveProduk = await Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) => TambahProduk(null, 'satuan')));
                      widget.saveProdukCallback(statusSaveProduk);
                    },
                    child: Text('Tambah Produk Baru',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.5)))
              ],
            ))
          ]),
        ));
  }
}
