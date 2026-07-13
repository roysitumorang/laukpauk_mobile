import 'package:flutter/material.dart';
import 'package:laukpauk/views/penjual/components/UpdateHarga/SatuanProduk.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class UpdateHargaItem extends StatefulWidget {
  final Map dataProduk;
  UpdateHargaItem(this.dataProduk);

  _UpdateHargaItemState createState() => _UpdateHargaItemState();
}

class _UpdateHargaItemState extends State<UpdateHargaItem> {
  Widget build(BuildContext context) {
    Map dataProduk = widget.dataProduk;

    return Card(
        elevation: 5,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 15, vertical: 20),
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.start,
                  children: [
                    Container(
                        width: SizeConfig.horizontalBlock * 15,
                        height: SizeConfig.horizontalBlock * 15,
                        decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(50),
                            image: DecorationImage(
                                fit: BoxFit.cover,
                                image: NetworkImage(ServerPath.baseURL +
                                    '/' +
                                    ServerPath.uploadGambarProduk +
                                    '/compress/' +
                                    dataProduk['foto'])))),
                    SizedBox(width: 15),
                    Expanded(
                      child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(dataProduk['nama'].toUpperCase(),
                                style: TextStyle(
                                    fontSize: SizeConfig.horizontalBlock * 4,
                                    fontWeight: FontWeight.w700)),
                            SizedBox(height: 3),
                            Text(dataProduk['namaKategoriProduk'],
                                style: TextStyle(
                                    color: CustomColors.textMuted,
                                    fontSize: SizeConfig.horizontalBlock * 3.5))
                          ]),
                    )
                  ]),
              SizedBox(height: 15),
              SatuanProduk(dataProduk['id'])
            ])));
  }
}
