import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/models/KategoriProdukModel.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/SemuaKategoriProduk.dart';
import 'package:laukpauk/views/pembeli/ListProduk.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

class Kategori extends StatefulWidget {
  _KategoriState createState() => _KategoriState();
}

class _KategoriState extends State<Kategori> {
  String _isThisKategoriFetchingProduct = ''; //id kategori

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Padding(
          padding: EdgeInsets.only(bottom: 5.0),
          child:
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
            Text('Kategori Produk',
                textAlign: TextAlign.left,
                style: TextStyle(
                    fontWeight: FontWeight.w500,
                    fontSize: SizeConfig.horizontalBlock * 4.0)),
            InkWell(
                onTap: () {
                  Navigator.push(context,
                      MaterialPageRoute(builder: (_) => SemuaKategoriProduk()));
                },
                child: Text('Lihat semua',
                    style: TextStyle(
                        color: CustomColors.textMuted,
                        fontSize: SizeConfig.horizontalBlock * 3.0)))
          ])),
      FutureBuilder<List>(
          future: KategoriProdukModel.getKategoriProduk(limitData: 7),
          builder: (context, snapshot) {
            SizeConfig().initSize(context);

            if (snapshot.hasData) {
              if (snapshot.data != null) {
                List kategoriProduk = snapshot.data!;
                if (kategoriProduk.length >= 1) {
                  return Container(
                    height: SizeConfig.horizontalBlock * 25,
                    child: ListView.builder(
                        scrollDirection: Axis.horizontal,
                        itemBuilder: (context, index) {
                          Map<String, dynamic> kategori = kategoriProduk[index];

                          String idKategoriProduk = kategori['id'];
                          String namaKategoriProduk = kategori['nama'];
                          String gambar = kategori['gambar'];

                          bool isDefaultImage =
                              gambar == DefaultImage.defaultKategoriProduk;

                          return Row(
                            children: [
                              InkWell(
                                onTap: () async {
                                  setState(() {
                                    _isThisKategoriFetchingProduct =
                                        idKategoriProduk;
                                  });

                                  List productInThisCategory =
                                      await KategoriProdukModel
                                          .getProdukInThisCategory(
                                              idKategoriProduk,
                                              select:
                                                  'id, idPenjual, namaToko, foto, nama, namaKategoriProduk');

                                  String title = 'Daftar Produk';
                                  String subTitle =
                                      'Berikut ini adalah produk di dalam kategori "${namaKategoriProduk.toUpperCase()}"';

                                  setState(() {
                                    _isThisKategoriFetchingProduct = '';
                                  });

                                  await Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => ListProduk(
                                              title: title,
                                              subTitle: subTitle,
                                              isReadyToShow: true,
                                              dataProduk: productInThisCategory,
                                              onRefresh: () async {})));
                                },
                                child: Stack(children: [
                                  Container(
                                      width: SizeConfig.horizontalBlock * 32.5,
                                      height: SizeConfig.horizontalBlock * 21.5,
                                      decoration: BoxDecoration(
                                          color: CustomColors.nearlyWhite,
                                          borderRadius:
                                              BorderRadius.circular(10),
                                          image: DecorationImage(
                                              fit: (isDefaultImage)
                                                  ? BoxFit.contain
                                                  : BoxFit.cover,
                                              image: NetworkImage(ServerPath
                                                      .baseURL +
                                                  '/' +
                                                  ServerPath
                                                      .uploadGambarKategoriProduk +
                                                  '/compress/' +
                                                  gambar)))),
                                  Positioned(
                                      left: 15,
                                      right: 15,
                                      bottom: 5,
                                      child: Center(
                                        child: Container(
                                            padding: EdgeInsets.symmetric(
                                                horizontal: 10, vertical: 5),
                                            decoration: BoxDecoration(
                                                color: CustomColors.nearlyWhite,
                                                borderRadius:
                                                    BorderRadius.circular(50)),
                                            child: SingleChildScrollView(
                                              scrollDirection: Axis.horizontal,
                                              child: Text(namaKategoriProduk,
                                                  textAlign: TextAlign.center,
                                                  style: TextStyle(
                                                      color: Colors.black87,
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          2.5)),
                                            )),
                                      )),
                                  (_isThisKategoriFetchingProduct ==
                                          idKategoriProduk)
                                      ? Positioned(
                                          top: 0,
                                          left: 0,
                                          bottom: 0,
                                          right: 0,
                                          child: Container(
                                            decoration: BoxDecoration(
                                                color: Color(0xAAFFFFFF)),
                                            child: Loading(
                                                circularProgressIndicatorSize:
                                                    Size(25.0, 25.0)),
                                          ))
                                      : SizedBox()
                                ]),
                              ),
                              SizedBox(width: 5)
                            ],
                          );
                        },
                        itemCount: kategoriProduk.length),
                  );
                }
              }
              return Container(
                width: MediaQuery.of(context).size.width,
                child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Image.asset(ClientPath.assetsImg + '/empty.png',
                          width: SizeConfig.horizontalBlock * 25),
                      SizedBox(height: 10),
                      Text('Kategori Produk Tidak Ada',
                          style: TextStyle(
                              color: CustomColors.textMuted,
                              fontSize: SizeConfig.horizontalBlock * 3.5))
                    ]),
              );
            } else {
              return Loading();
            }
          })
    ]);
  }
}
