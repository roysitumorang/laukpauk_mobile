import 'package:flutter/material.dart';
import 'package:laukpauk/models/KontenModel.dart';

import 'package:laukpauk/views/pembeli/components/HomePage/KontenItem.dart';
import 'package:laukpauk/views/pembeli/ListProduk.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';

class Konten extends StatelessWidget {
  final Map<String, dynamic> detailKonten;
  Konten(this.detailKonten);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String _idKonten = detailKonten['id'];
    String _namaDivisi = detailKonten['namaDivisi'];
    String _jenisItem = detailKonten['jenisItem'];
    String _jenisItemLC = _jenisItem.toLowerCase();

    return Padding(
      padding: EdgeInsets.only(bottom: 15),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(_namaDivisi,
                style: TextStyle(
                    fontWeight: FontWeight.w500,
                    fontSize: SizeConfig.horizontalBlock * 4.0)),
            (_jenisItemLC == 'produk')
                ? InkWell(
                    onTap: () async {
                      List getProdukKontenItem =
                          await KontenModel.getProdukKonten(_idKonten);

                      String title = _namaDivisi.toUpperCase();
                      String subTitle =
                          'Berikut ini adalah produk - produk ${_namaDivisi.toUpperCase()}';
                      Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) => ListProduk(
                                  title: title,
                                  subTitle: subTitle,
                                  dataProduk: getProdukKontenItem,
                                  isReadyToShow: true,
                                  onRefresh: null)));
                    },
                    child: Text('Lihat Semua',
                        style: TextStyle(
                            color: CustomColors.darkGreen,
                            fontWeight: FontWeight.w500,
                            fontSize: SizeConfig.horizontalBlock * 3.0)),
                  )
                : SizedBox(),
          ],
        ),
        SizedBox(height: 10),
        KontenItem(detailKonten),
      ]),
    );
  }
}
