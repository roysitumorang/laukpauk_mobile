import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/PesananSaya.dart';

import 'package:laukpauk/views/penjual/TokoSaya.dart';
import 'package:laukpauk/views/pembeli/RequestJadiPenjual.dart';
import 'package:laukpauk/views/pembeli/ProfilSaya.dart';

class Transaksi extends StatefulWidget {
  final Map<String, dynamic> dataPenjual;
  Transaksi(this.dataPenjual);
  _TransaksiState createState() => _TransaksiState();
}

class _TransaksiState extends State<Transaksi> {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> dataPenjual = widget.dataPenjual;
    bool isSeller = (dataPenjual['isSeller'] == '1');

    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 15),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text('Transaksi dan Akun',
            style: CustomText.textMD().copyWith(fontWeight: FontWeight.bold)),
        SizedBox(height: 15.0),
        Row(children: [
          InkWell(
            onTap: () {
              Navigator.push(
                  context, MaterialPageRoute(builder: (_) => PesananSaya()));
            },
            child: Container(
              width: SizeConfig.horizontalBlock * 20,
              child: Column(children: [
                Image.asset(
                    ClientPath.assetsImg +
                        '/icons/pembeli/belanjaan-hari-ini.png',
                    width: SizeConfig.horizontalBlock * 9,
                    height: SizeConfig.horizontalBlock * 9),
                SizedBox(height: 10),
                Text('Belanjaan Hari Ini',
                    textAlign: TextAlign.center,
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25))
              ]),
            ),
          ),
          InkWell(
            onTap: () {
              Navigator.push(
                  context,
                  MaterialPageRoute(
                      builder: (_) => PesananSaya(statusTransaksi: 2)));
            },
            child: Container(
              width: SizeConfig.horizontalBlock * 20,
              child: Column(children: [
                Image.asset(
                    ClientPath.assetsImg + '/icons/pembeli/histori-belanja.png',
                    width: SizeConfig.horizontalBlock * 9,
                    height: SizeConfig.horizontalBlock * 9),
                SizedBox(height: 10),
                Text('Histori Belanja Saya',
                    textAlign: TextAlign.center,
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25))
              ]),
            ),
          ),
          InkWell(
            onTap: () {
              if (isSeller) {
                Navigator.pushAndRemoveUntil(
                    context,
                    MaterialPageRoute(builder: (_) => TokoSaya()),
                    (route) => false);
              } else {
                Navigator.push(context,
                    MaterialPageRoute(builder: (_) => RequestJadiPenjual()));
              }
            },
            child: Container(
              width: SizeConfig.horizontalBlock * 20,
              child: Column(children: [
                Image.asset(ClientPath.assetsImg + '/store.png',
                    width: SizeConfig.horizontalBlock * 9,
                    height: SizeConfig.horizontalBlock * 9),
                SizedBox(height: 10),
                Text((isSeller) ? 'Lihat Toko Saya' : 'Jualan Yuk!',
                    textAlign: TextAlign.center,
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25))
              ]),
            ),
          ),
          InkWell(
            onTap: () {
              Navigator.push(
                  context, MaterialPageRoute(builder: (_) => ProfilSaya()));
            },
            child: Container(
              width: SizeConfig.horizontalBlock * 20,
              child: Column(children: [
                Image.asset(ClientPath.assetsImg + '/user-icon.png',
                    width: SizeConfig.horizontalBlock * 9,
                    height: SizeConfig.horizontalBlock * 9),
                SizedBox(height: 10),
                Text('Atur Akun Saya',
                    textAlign: TextAlign.center,
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25))
              ]),
            ),
          )
        ], mainAxisAlignment: MainAxisAlignment.spaceBetween),
        SizedBox(height: 10)
      ]),
    );
  }
}
