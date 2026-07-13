import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/SplashScreen.dart';
import 'package:shared_preferences/shared_preferences.dart';

class OTPRegistrasi extends StatefulWidget {
  final Widget? routeTo;
  OTPRegistrasi({this.routeTo});
  _OTPRegistrasi createState() => _OTPRegistrasi();
}

class _OTPRegistrasi extends State<OTPRegistrasi> {
  TextEditingController otpCodeController = TextEditingController();

  void dispose() {
    otpCodeController.dispose();
    super.dispose();
  }

  Future<void> konfirmasiOTP() async {
    String idUser = await LocalStorage.getCurrentUserID();

    Map<String, dynamic> otpData = {
      'idUser': idUser,
      'otp': otpCodeController.text
    };
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: otpData);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Auth/konfirmasiOTP');
    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool isOTPSuccess = dataObject['statusKonfirmasiOTP'];
      if (isOTPSuccess) {
        SharedPreferences _sP = await SharedPreferences.getInstance();

        _sP.setBool('otpHasConfirmed', true);

        String authToken = dataObject['returnData']['authToken'];
        _sP.setString('authToken', authToken);
      }
      String _title = 'Konfirmasi OTP';
      String _subTitle = (isOTPSuccess)
          ? 'Konfirmasi OTP Berhasil ! Anda sudah terautentikasi, jadi anda tidak perlu login kembali.'
          : 'Konfirmasi OTP gagal, silahkan ulangi kembali !';
      Widget _actions =
          Row(mainAxisAlignment: MainAxisAlignment.center, children: [
        (isOTPSuccess)
            ? TextButton(
                child: Text('Masuk'),
                onPressed: () {
                  Navigator.pushAndRemoveUntil(
                      context,
                      MaterialPageRoute(
                          builder: (_) => (widget.routeTo != null)
                              ? widget.routeTo!
                              : SplashScreen()),
                      (route) => false);
                })
            : TextButton(
                child: Text('OK'),
                onPressed: () {
                  Navigator.pop(context);
                })
      ]);

      await CustomAlert.textComponent(context,
          title: _title, subTitle: _subTitle, actions: _actions);
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        body: SingleChildScrollView(
            child: Container(
      padding: EdgeInsets.symmetric(horizontal: 10, vertical: 15),
      height: MediaQuery.of(context).size.height,
      width: MediaQuery.of(context).size.width,
      decoration: BoxDecoration(color: Color(0xFFF9F9F9)),
      child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Image.asset(ClientPath.assetsImg + '/laukpauk.png',
                width: MediaQuery.of(context).size.width - 100),
            SizedBox(height: 50),
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 25),
              child: Container(
                padding: EdgeInsets.symmetric(horizontal: 30),
                decoration: BoxDecoration(
                    color: CustomColors.lightGreen,
                    borderRadius: BorderRadius.circular(50)),
                child: TextField(
                  style: TextStyle(color: CustomColors.darkText),
                  controller: otpCodeController,
                  keyboardType: TextInputType.number,
                  cursorColor: CustomColors.darkText,
                  textAlignVertical: TextAlignVertical.center,
                  textAlign: TextAlign.left,
                  decoration: InputDecoration(
                      border: InputBorder.none,
                      hintText: 'Kode OTP',
                      hintStyle: TextStyle(color: CustomColors.darkText)),
                ),
              ),
            ),
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 25, vertical: 10),
              child: Text(
                  'Silahkan masukkan kode OTP yang dikirim ke nomor yang anda daftarkan',
                  textAlign: TextAlign.center),
            ),
            SizedBox(height: 15),
            Padding(
                padding: EdgeInsets.symmetric(
                    horizontal: SizeConfig.horizontalBlock * 10.0),
                child:
                    Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                  CustomButton.button(context, 'Konfirmasi OTP', () {
                    konfirmasiOTP();
                  }, color: CustomButton.themeColor, isPill: true),
                  SizedBox(width: SizeConfig.horizontalBlock * 1.5),
                  CustomButton.button(context, 'Lewati', () async {
                    await LocalStorage.lewatiOTPRegistrasi();
                    await Navigator.pushAndRemoveUntil(
                        context,
                        MaterialPageRoute(builder: (_) => SplashScreen()),
                        (route) => false);
                  },
                      color: CustomButton.themeColor,
                      isPill: true,
                      outline: true)
                ]))
          ]),
    )));
  }
}
