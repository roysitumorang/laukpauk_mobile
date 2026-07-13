import 'package:flutter/material.dart';
import 'package:laukpauk/views/penjual/PesananMasuk.dart';
import 'package:laukpauk/views/pembeli/UserAndDashboard.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/penjual/HistoriPenjualan.dart';

class TransaksiPenjual extends StatefulWidget {
  _TransaksiPenjualState createState() => _TransaksiPenjualState();
}

class _TransaksiPenjualState extends State<TransaksiPenjual> {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Card(
        elevation: 5,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 15, vertical: 10),
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text('Transaksi Penjual',
                style: TextStyle(
                    fontSize: SizeConfig.horizontalBlock * 4,
                    fontWeight: FontWeight.w700)),
            SizedBox(height: 15),
            Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  InkWell(
                    onTap: () async {
                      await Navigator.push(context,
                          MaterialPageRoute(builder: (_) => PesananMasuk()));
                    },
                    child: Container(
                      width: SizeConfig.horizontalBlock * 17.5,
                      child: Column(children: [
                        Image.asset(
                            ClientPath.assetsImg + '/icons/orderan-baru.png',
                            width: SizeConfig.horizontalBlock * 9,
                            height: SizeConfig.horizontalBlock * 9),
                        SizedBox(height: 10),
                        Text('Orderan Baru Masuk',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3)),
                      ]),
                    ),
                  ),
                  InkWell(
                    onTap: () async {
                      await Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) =>
                                  PesananMasuk(statusTransaksi: 1)));
                    },
                    child: Container(
                      width: SizeConfig.horizontalBlock * 17.5,
                      child: Column(children: [
                        Image.asset(
                            ClientPath.assetsImg + '/icons/delivery.png',
                            width: SizeConfig.horizontalBlock * 9,
                            height: SizeConfig.horizontalBlock * 9),
                        SizedBox(height: 10),
                        Text('Dalam Perjalanan',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3)),
                      ]),
                    ),
                  ),
                  InkWell(
                    onTap: () async {
                      await Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) =>
                                  PesananMasuk(statusTransaksi: 2)));
                    },
                    child: Container(
                      width: SizeConfig.horizontalBlock * 17.5,
                      child: Column(children: [
                        Image.asset(
                            ClientPath.assetsImg + '/icons/completed.png',
                            width: SizeConfig.horizontalBlock * 9,
                            height: SizeConfig.horizontalBlock * 9),
                        SizedBox(height: 10),
                        Text('Pesanan Diterima',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3)),
                      ]),
                    ),
                  ),
                  InkWell(
                    onTap: () {
                      Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) => HistoriPenjualan()));
                    },
                    child: Container(
                      width: SizeConfig.horizontalBlock * 17.5,
                      child: Column(children: [
                        Image.asset(
                            ClientPath.assetsImg + '/icons/penjualan.png',
                            width: SizeConfig.horizontalBlock * 9,
                            height: SizeConfig.horizontalBlock * 9),
                        SizedBox(height: 10),
                        Text('Histori Penjualan',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3)),
                      ]),
                    ),
                  ),
                  InkWell(
                    onTap: () async {
                      await Navigator.pushAndRemoveUntil(
                          context,
                          MaterialPageRoute(builder: (_) => UserAndDashboard()),
                          (route) => false);
                    },
                    child: Container(
                      width: SizeConfig.horizontalBlock * 17.5,
                      child: Column(children: [
                        Image.asset(ClientPath.assetsImg + '/user-icon.png',
                            width: SizeConfig.horizontalBlock * 9,
                            height: SizeConfig.horizontalBlock * 9),
                        SizedBox(height: 10),
                        Text('Akun Saya',
                            textAlign: TextAlign.center,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3)),
                      ]),
                    ),
                  )
                ]),
            SizedBox(height: 10)
          ]),
        ));
  }
}
