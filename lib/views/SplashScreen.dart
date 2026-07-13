import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/models/SplashScreenModel.dart';
import 'package:laukpauk/models/TransaksiModel.dart';

import 'package:laukpauk/views/IntroPage.dart';
import 'package:laukpauk/views/pembeli/OTPRegistrasi.dart';

import 'package:laukpauk/views/pembeli/HomePage.dart';

class SplashScreen extends StatefulWidget {
  final bool removeTransactionSession;
  final Widget? routeTo;
  SplashScreen({this.routeTo, this.removeTransactionSession = false});
  _SplashScreenState createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with WidgetsBindingObserver {
  void initState() {
    super.initState();
      WidgetsBinding.instance.addObserver(this);
      WidgetsBinding.instance.addPostFrameCallback((_) {
        _initialization();
      });
  }

  Future<void> _initialization() async {
    Widget? _routeTo = widget.routeTo;
    bool _removeTransactionSession = widget.removeTransactionSession;

    if (_removeTransactionSession) {
      await TransaksiModel.removeTransactionSession(context);
    } else {
      if (_routeTo != null) {
        await Future.delayed(Duration(milliseconds: 3000));
        await Navigator.pushAndRemoveUntil(context,
            MaterialPageRoute(builder: (_) => _routeTo), (route) => false);
      } else {
        await Future.delayed(Duration(milliseconds: 1500));
        bool _hasAppStarted = await SplashScreenModel.checkHasAppStarted();

        if (_hasAppStarted) {
          bool _otpHasSent = await SplashScreenModel.checkHasOTPSent();
          bool _otpHasConfirmed =
              await SplashScreenModel.checkHasOTPConfirmed();
          bool _isOTPRegistrasiPassed =
              await LocalStorage.isOTPRegistrasiPassed();

          if (_otpHasSent && (!_otpHasConfirmed || _isOTPRegistrasiPassed)) {
            await Navigator.pushAndRemoveUntil(
                context,
                MaterialPageRoute(
                    builder: (_) => (_isOTPRegistrasiPassed)
                        ? HomePage()
                        : OTPRegistrasi()),
                (route) => false);
          } else {
            await Navigator.pushAndRemoveUntil(
                context,
                MaterialPageRoute(builder: (_) => HomePage()),
                (route) => false);
          }
        } else {
          await Navigator.pushAndRemoveUntil(context,
              MaterialPageRoute(builder: (_) => IntroPage()), (route) => false);
        }
      }
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        body: Center(
            child: Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Image.asset(ClientPath.assetsImg + '/laukpauk-icon.png',
            width: SizeConfig.horizontalBlock * 30.0),
        SizedBox(height: 20.0),
        Text('Kalau Apakali, Laukpauk Aja',
            style: TextStyle(fontSize: 12.5, fontFamily: 'Poppins'))
      ],
    )));
  }
}
