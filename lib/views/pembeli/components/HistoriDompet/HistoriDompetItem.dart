import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class HistoriDompetItem extends StatelessWidget {
  final int nomor;
  final Map dataRiwayatDompet;
  HistoriDompetItem(this.nomor, this.dataRiwayatDompet);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    int nilaiDompet = int.parse(dataRiwayatDompet['nilai']);

    return Container(
        margin: EdgeInsets.only(bottom: 15),
        padding: EdgeInsets.symmetric(horizontal: 10),
        child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(nomor.toString() + '.',
                  style: TextStyle(fontWeight: FontWeight.w700)),
              SizedBox(width: 15),
              Expanded(
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                    (dataRiwayatDompet['jenis'].length >= 1)
                        ? Text(dataRiwayatDompet['jenis'].toUpperCase(),
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 4,
                                fontWeight: FontWeight.w700))
                        : Text('Tanpa Jenis Dompet',
                            style: TextStyle(
                                color: CustomColors.textMuted,
                                fontStyle: FontStyle.italic,
                                fontSize: SizeConfig.horizontalBlock * 4,
                                fontWeight: FontWeight.w700)),
                    SizedBox(height: 5),
                    (dataRiwayatDompet['keterangan'].length >= 1)
                        ? Text(dataRiwayatDompet['keterangan'],
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.25))
                        : Text('Tanpa Keterangan',
                            style: TextStyle(
                                color: CustomColors.textMuted,
                                fontStyle: FontStyle.italic,
                                fontSize: SizeConfig.horizontalBlock * 3.25,
                                fontWeight: FontWeight.w700)),
                    SizedBox(height: 5),
                    Text('Pada ' + dataRiwayatDompet['createdAt'],
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.25))
                  ])),
              Text(Currency.formatRupiah(nilaiDompet, 0),
                  style: TextStyle(
                      fontWeight: FontWeight.w700,
                      color: (nilaiDompet >= 0)
                          ? CustomColors.green
                          : CustomColors.textDanger,
                      fontSize: SizeConfig.horizontalBlock * 4))
            ]));
  }
}
