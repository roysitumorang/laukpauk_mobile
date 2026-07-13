import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/SplashScreen.dart';
import 'package:laukpauk/views/pembeli/DetailTransaksi.dart';
import 'package:laukpauk/models/TransaksiModel.dart';

class BuatOrderanBerhasil extends StatefulWidget {
  final Map<String, dynamic> returnedDataFromCheckout;
  final bool removeTransactionSession;
  BuatOrderanBerhasil(
      {required this.returnedDataFromCheckout,
      this.removeTransactionSession = false});

  _BuatOrderanBerhasilState createState() => _BuatOrderanBerhasilState();
}

class _BuatOrderanBerhasilState extends State<BuatOrderanBerhasil> {
  void initState() {
    super.initState();
    _initialization();
  }

  void _initialization() async {
    bool removeTransactionSession = widget.removeTransactionSession;
    if (removeTransactionSession) {
      await TransaksiModel.removeTransactionSession(context);
    }
  }

  void didUpdateWidget(BuatOrderanBerhasil oldBuatOrderanBerhasil) {
    super.didUpdateWidget(oldBuatOrderanBerhasil);
    _initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    TextStyle subTitleSize =
        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.75);

    Map<String, dynamic> returnedDataFromCheckout =
        widget.returnedDataFromCheckout;

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        body: Center(
          child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 15, vertical: 25),
            child:
                Column(mainAxisAlignment: MainAxisAlignment.center, children: [
              Image.asset(ClientPath.assetsImg + '/buatOrderanBerhasil.png',
                  width: SizeConfig.horizontalBlock * 50),
              SizedBox(height: 15),
              Padding(
                padding: EdgeInsets.symmetric(
                    horizontal: SizeConfig.horizontalBlock * 19.0),
                child: Text(
                    'Yeayy! Orderan berhasil. Pesanan sedang diproses ya mak...!',
                    style: subTitleSize,
                    textAlign: TextAlign.center),
              ),
              SizedBox(height: 25.0),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CustomButton.button(context, 'Belanja Lagi', () {
                    Navigator.pushAndRemoveUntil(
                        context,
                        MaterialPageRoute(builder: (_) => SplashScreen()),
                        (route) => false);
                  }, color: CustomButton.themeColor, isPill: true),
                  SizedBox(width: 7.5),
                  CustomButton.button(context, 'Lihat Detail Transaksi', () {
                    String idTransaksi =
                        returnedDataFromCheckout['responseCheckout']
                                ['idTransaksi']
                            .toString();

                    Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (_) => DetailTransaksi(idTransaksi)));
                  },
                      color: CustomButton.themeColor,
                      isPill: true,
                      outline: true)
                ],
              )
            ]),
          ),
        ));
  }
}
