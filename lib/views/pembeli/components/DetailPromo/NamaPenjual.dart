import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/Penjual.dart';

class NamaPenjual extends StatelessWidget {
  final Map<String, dynamic> detailPenjual;
  NamaPenjual(this.detailPenjual);

  Widget build(BuildContext context) {
    String namaToko = detailPenjual['namaToko'];
    String idPenjual = detailPenjual['idToko'];

    return Row(mainAxisAlignment: MainAxisAlignment.start, children: [
      Image.asset(ClientPath.assetsImg + '/store.png',
          width: SizeConfig.horizontalBlock * 7.5),
      SizedBox(width: 10),
      Expanded(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(mainAxisAlignment: MainAxisAlignment.start, children: [
              Text('Penjual',
                  style: TextStyle(
                      color: CustomColors.textMuted,
                      fontSize: SizeConfig.horizontalBlock * 3.5)),
              SizedBox(width: 5),
              InkWell(
                onTap: () {
                  Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (_) => Penjual(id: idPenjual)));
                },
                child: Text(
                    (namaToko.length >= 1)
                        ? namaToko
                        : 'Penjual Tidak Diketahui',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3.5,
                        color: CustomColors.textInfo,
                        fontWeight: FontWeight.w600)),
              )
            ]),
          ),
          Text('Klik nama penjual untuk melihat seluruh produk penjual',
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 2.5)),
        ]),
      )
    ]);
  }
}
