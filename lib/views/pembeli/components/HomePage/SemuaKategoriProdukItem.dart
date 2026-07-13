import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/models/KategoriProdukModel.dart';

import 'package:laukpauk/views/pembeli/ListProduk.dart';

class SemuaKategoriProdukItem extends StatefulWidget {
  final Map<String, dynamic> dataKategori;
  SemuaKategoriProdukItem(this.dataKategori);

  @override
  _SemuaKategoriProdukItemState createState() =>
      _SemuaKategoriProdukItemState();
}

class _SemuaKategoriProdukItemState extends State<SemuaKategoriProdukItem> {
  List productInThisCategoryState = [];

  Widget build(BuildContext context) {
    String idKategoriProduk = widget.dataKategori['id'];
    String nama = widget.dataKategori['nama'];
    String gambar = widget.dataKategori['gambar'];

    return InkWell(
      onTap: () async {
        List productInThisCategory =
            await KategoriProdukModel.getProdukInThisCategory(idKategoriProduk);

        setState(() {
          productInThisCategoryState = productInThisCategory;
        });

        String title = 'Daftar Produk';
        String subTitle =
            'Berikut ini adalah produk di dalam kategori "${nama.toUpperCase()}"';

        Navigator.push(
            context,
            MaterialPageRoute(
                builder: (_) => ListProduk(
                    title: title,
                    subTitle: subTitle,
                    isReadyToShow: true,
                    dataProduk: productInThisCategoryState,
                    onRefresh: () async {
                      List productInThisCategory =
                          await KategoriProdukModel.getProdukInThisCategory(
                              idKategoriProduk);

                      setState(() {
                        productInThisCategoryState = productInThisCategory;
                      });
                    })));
      },
      child: Stack(
        children: [
          Container(
              decoration: BoxDecoration(
                  color: CustomColors.nearlyWhite,
                  borderRadius: BorderRadius.circular(10),
                  image: DecorationImage(
                      fit: BoxFit.cover,
                      image: NetworkImage(ServerPath.baseURL +
                          '/' +
                          ServerPath.uploadGambarKategoriProduk +
                          '/compress/' +
                          gambar)))),
          Container(
              decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(10),
                  color: Color.fromRGBO(0, 0, 0, 0.4))),
          Positioned(
              bottom: 7.5,
              left: 7.5,
              right: 7.5,
              child: Text(nama,
                  style: TextStyle(
                      fontSize: SizeConfig.horizontalBlock * 3.0,
                      color: CustomColors.nearlyWhite)))
        ],
      ),
    );
  }
}
