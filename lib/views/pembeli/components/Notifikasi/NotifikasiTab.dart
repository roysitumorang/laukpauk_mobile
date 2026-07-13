import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/CustomColors.dart';

class NotifikasiTab extends StatelessWidget {
  final String currentJenisNotifikasi;
  final int jumlahNotifikasiGlobal, jumlahNotifikasiKhusus;
  final Function(String jenisNotifikasiSelected) onTabClicked;

  NotifikasiTab(this.currentJenisNotifikasi, this.onTabClicked,
      {this.jumlahNotifikasiGlobal = 0, this.jumlahNotifikasiKhusus = 0});
  Widget build(BuildContext context) {
    String jenisNotifikasi = currentJenisNotifikasi.toLowerCase();

    return Row(mainAxisAlignment: MainAxisAlignment.spaceEvenly, children: [
      Expanded(
          child: InkWell(
        onTap: () {
          onTabClicked('update');
        },
        child: Container(
            padding: EdgeInsets.symmetric(vertical: 15.0),
            decoration: BoxDecoration(
                color: Color(
                    (jenisNotifikasi == 'update') ? 0x153A5160 : 0x9966CC99)),
            child: Center(
                child: Text('Info',
                    style: TextStyle(
                        fontWeight: (jenisNotifikasi == 'update')
                            ? null
                            : FontWeight.w700,
                        color: (jenisNotifikasi == 'update')
                            ? CustomColors.textMuted
                            : CustomColors.nearlyWhite)))),
      )),
      Expanded(
          child: InkWell(
        onTap: () {
          onTabClicked('khususuntukkamu');
        },
        child: Container(
            padding: EdgeInsets.symmetric(vertical: 15.0),
            decoration: BoxDecoration(
                color: Color((jenisNotifikasi == 'khususuntukkamu')
                    ? 0x153A5160
                    : 0x9966CC99)),
            child: Center(
                child: Text('Notifikasi',
                    style: TextStyle(
                        fontWeight: (jenisNotifikasi == 'khususuntukkamu')
                            ? null
                            : FontWeight.w700,
                        color: (jenisNotifikasi == 'khususuntukkamu')
                            ? CustomColors.textMuted
                            : CustomColors.nearlyWhite)))),
      ))
    ]);
  }
}
