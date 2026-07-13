import 'package:flutter/material.dart';
import 'package:laukpauk/models/TransaksiModel.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/SplashScreen.dart';

class DoPayment extends StatefulWidget {
  _DoPaymentState createState() => _DoPaymentState();
}

class _DoPaymentState extends State<DoPayment> {
  void initState() {
    super.initState();
    _initialization();
  }

  Future<void> _initialization() async {
    await TransaksiModel.removeTransactionSession(context);
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    TextStyle titleSize = TextStyle(
        fontSize: SizeConfig.horizontalBlock * 4.25,
        fontWeight: FontWeight.w700);
    TextStyle subTitleSize =
        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25);

    return Scaffold(
        backgroundColor: CustomColors.aliceBlue,
        body: Center(
          child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 15, vertical: 25),
            child:
                Column(mainAxisAlignment: MainAxisAlignment.center, children: [
              Image.asset(ClientPath.assetsImg + '/ovo.png',
                  width: SizeConfig.horizontalBlock * 25),
              SizedBox(height: 15),
              Text('Lakukan Pembayaran', style: titleSize),
              Padding(
                padding: EdgeInsets.only(top: 5),
                child: Text(
                    'Silahkan buka aplikasi OVO yang terinstal di perangkat anda untuk melakukan pembayaran',
                    style: subTitleSize,
                    textAlign: TextAlign.center),
              ),
              SizedBox(height: 15),
              CustomButton.button(context, 'Belanja Lagi', () {
                Navigator.pushAndRemoveUntil(
                    context,
                    MaterialPageRoute(builder: (_) => SplashScreen()),
                    (route) => false);
              }, color: CustomColors.green, isPill: true)
            ]),
          ),
        ));
  }
}
