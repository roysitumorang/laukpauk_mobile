import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/HomePage.dart';
import 'package:laukpauk/views/pembeli/KirimUlangOTP.dart';
import 'package:laukpauk/views/pembeli/OTPRegistrasi.dart';
import 'package:laukpauk/views/pembeli/Checkout.dart';

class OTPNotCompleted extends StatelessWidget {
  final Widget? routeTo;
  OTPNotCompleted({this.routeTo});
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Widget _routeTo = (routeTo == null) ? Checkout() : routeTo!;

    return Scaffold(
        body: Center(
            child: Padding(
      padding:
          EdgeInsets.symmetric(horizontal: SizeConfig.horizontalBlock * 10.0),
      child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
        Image.asset(ClientPath.assetsImg + '/empty.png',
            width: SizeConfig.horizontalBlock * 30.0),
        SizedBox(height: SizeConfig.horizontalBlock * 5.0),
        Text('OTP Belum Terverifikasi',
            style: TextStyle(
                fontWeight: FontWeight.w500,
                fontSize: SizeConfig.horizontalBlock * 4.0)),
        SizedBox(height: 10.0),
        Text('Silahkan masukkan ulang kode otp yang dikirimkan ke HP anda',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)),
        SizedBox(height: SizeConfig.horizontalBlock * 10.0),
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
          Expanded(
            child: CustomButton.button(context, 'Verifikasi OTP', () {
              Navigator.pushAndRemoveUntil(
                  context,
                  MaterialPageRoute(
                      builder: (_) => OTPRegistrasi(routeTo: _routeTo)),
                  (route) => false);
            }, color: CustomColors.green, isPill: true, isBlock: true),
          ),
          SizedBox(width: SizeConfig.horizontalBlock * 1.5),
          Expanded(
            child: CustomButton.button(context, 'Kirim Ulang ', () {
              Navigator.pushAndRemoveUntil(
                  context,
                  MaterialPageRoute(
                      builder: (_) => KirimUlangOTP(routeTo: _routeTo)),
                  (route) => false);
            }, color: CustomColors.textInfo, isPill: true, isBlock: true),
          )
        ]),
        SizedBox(height: SizeConfig.horizontalBlock * 2.5),
        CustomButton.button(context, 'Home ', () {
          Navigator.pushAndRemoveUntil(context,
              MaterialPageRoute(builder: (_) => HomePage()), (route) => false);
        },
            color: CustomColors.green,
            outline: true,
            isPill: true,
            isBlock: true)
      ]),
    )));
  }
}
