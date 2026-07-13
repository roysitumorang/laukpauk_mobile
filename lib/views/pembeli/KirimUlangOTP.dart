import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/views/pembeli/OTPRegistrasi.dart';
import 'package:laukpauk/views/pembeli/Checkout.dart';
import 'package:laukpauk/models/UserModel.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class KirimUlangOTP extends StatefulWidget {
  final Widget? routeTo;
  KirimUlangOTP({this.routeTo});
  _KirimUlangOTP createState() => _KirimUlangOTP();
}

class _KirimUlangOTP extends State<KirimUlangOTP> {
  TextEditingController _nomorTeleponController = TextEditingController();

  bool _isButtonKirimOTPBusy = false;

  void initState() {
    super.initState();
    _initialization();
  }

  Future<void> _initialization() async {
    dynamic _detailUser = await UserModel.getDetailActiveUser();
    if (_detailUser != false) {
      String _nomorTelepon = _detailUser['nomorHP'];
      _nomorTeleponController.text = _nomorTelepon;
    }
  }

  Future<Map<String, dynamic>?> _kirimOTP() async {
    String _nomorTelepon = _nomorTeleponController.text;
    Map<String, dynamic> _httpBody = {'nomorTelepon': _nomorTelepon};

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: _httpBody);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Auth/resendOTP?' + _params);
    Response _httpResponse = await post(_url, body: _body);
    if (_httpResponse.statusCode == 200) {
      Map<String, dynamic> _decodedBody = jsonDecode(_httpResponse.body);

      return _decodedBody;
    }

    return null;
  }

  void dispose() {
    _nomorTeleponController.dispose();
    super.dispose();
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
                  controller: _nomorTeleponController,
                  keyboardType: TextInputType.number,
                  cursorColor: CustomColors.darkText,
                  textAlignVertical: TextAlignVertical.center,
                  textAlign: TextAlign.left,
                  decoration: InputDecoration(
                      border: InputBorder.none,
                      hintText: 'Nomor Telepon',
                      hintStyle: TextStyle(color: CustomColors.darkText)),
                ),
              ),
            ),
            SizedBox(height: 15),
            Padding(
                padding: EdgeInsets.symmetric(
                    horizontal: SizeConfig.horizontalBlock * 7.5),
                child:
                    Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                  CustomButton.button(context, 'Kirim OTP', () async {
                    setState(() {
                      _isButtonKirimOTPBusy = true;
                    });

                    Map<String, dynamic>? _responseResendOTP =
                        await _kirimOTP();

                    Duration _snackBarDuration = Duration(seconds: 3);

                    if (_responseResendOTP != null) {
                      bool _statusResendOTP =
                          _responseResendOTP['statusResendOTP'];

                      if (_statusResendOTP) {
                        Navigator.pushAndRemoveUntil(
                            context,
                            MaterialPageRoute(
                                builder: (_) =>
                                    OTPRegistrasi(routeTo: Checkout())),
                            (route) => false);
                      } else {
                        String _messageResendOTP =
                            _responseResendOTP['messageResendOTP'];
                        SnackBar _otpSnackbar = SnackBar(
                            duration: _snackBarDuration,
                            content: Text(_messageResendOTP));
                        ScaffoldMessenger.of(context)
                            .showSnackBar(_otpSnackbar);
                      }
                    } else {
                      SnackBar _otpSnackbar = SnackBar(
                          duration: _snackBarDuration,
                          content: Text(
                              'Sistem gagal melakukan operasi. Silahkan ulangi lagi!'));
                      ScaffoldMessenger.of(context).showSnackBar(_otpSnackbar);
                    }

                    setState(() {
                      _isButtonKirimOTPBusy = false;
                    });
                  },
                      color: CustomButton.themeColor,
                      isPill: true,
                      isBusy: _isButtonKirimOTPBusy),
                ])),
            SizedBox(height: SizeConfig.horizontalBlock * 10.0),
            Padding(
              padding: EdgeInsets.symmetric(
                  horizontal: SizeConfig.horizontalBlock * 7.5),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Note',
                      style: TextStyle(
                          fontSize: SizeConfig.horizontalBlock * 3.75,
                          fontWeight: FontWeight.w700)),
                  SizedBox(height: 10.0),
                  Text(
                      'Verifikasi kode OTP hanya bisa dilakukan sekali dalam sehari',
                      textAlign: TextAlign.justify,
                      style: TextStyle(
                          fontSize: SizeConfig.horizontalBlock * 3.25)),
                  //     Text(
                  // 'Data nomor telepon anda akan diubah sesuai dengan nomor telepon yang anda masukkan jika otp anda sukses, jika gagal nomor telepon anda tetap nomor sebelumnya',
                  // textAlign: TextAlign.justify,
                  // style: TextStyle(
                  //     fontSize: SizeConfig.horizontalBlock * 3.25)),
                  // SizedBox(height: 5.0),
                  // Text(
                  //     'Anda hanya dapat mengirim ulang kode otp satu kali dalam 24 jam.',
                  //     textAlign: TextAlign.justify,
                  //     style: TextStyle(
                  //         fontSize: SizeConfig.horizontalBlock * 2.75)),
                ],
              ),
            ),
            SizedBox(height: SizeConfig.horizontalBlock * 3.0),
          ]),
    )));
  }
}
