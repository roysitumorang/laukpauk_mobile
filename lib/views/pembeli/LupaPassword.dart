import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/Register.dart';
import 'package:laukpauk/views/pembeli/Login.dart';

class LupaPassword extends StatefulWidget {
  _LupaPassword createState() => _LupaPassword();
}

class _LupaPassword extends State<LupaPassword> {
  TextEditingController nomorHPController = TextEditingController();

  recoveryPassword() async {
    Map<String, String> lupaPasswordData = {'nomorHP': nomorHPController.text};
    Map<String, dynamic> _body =
        await HTTPRequest.postBody(body: lupaPasswordData);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Auth/lupaPassword');
    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      Map recoveryPassword = dataObject;

      bool isRecoveryPasswordSuccess =
          (recoveryPassword['statusRecoveryPassword'] == true);

      String _title = 'Recovery Password';
      String _subTitle = (isRecoveryPasswordSuccess)
          ? 'Password baru telah dikirimkan melalui SMS. Silahkan login ulang menggunakan password baru Anda'
          : recoveryPassword['returnData']['message'];
      Widget _actions =
          Row(mainAxisAlignment: MainAxisAlignment.center, children: [
        TextButton(
            onPressed: () {
              Navigator.pop(context);
            },
            child: Text('OK')),
        (isRecoveryPasswordSuccess)
            ? TextButton(
                onPressed: () {
                  Navigator.pushReplacement(
                      context, MaterialPageRoute(builder: (_) => Login()));
                },
                child: Text('Login'))
            : SizedBox()
      ]);

      await CustomAlert.textComponent(context,
          title: _title, subTitle: _subTitle, actions: _actions);
    }
  }

  void dispose() {
    nomorHPController.dispose();
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
            // Image.asset(ClientPath.assetsImg + '/recovery-password.png',
            //     width: 85),
            // SizedBox(height: 40),
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 25),
              child: Container(
                padding: EdgeInsets.symmetric(horizontal: 30),
                decoration: BoxDecoration(
                    color: CustomColors.lightGreen,
                    borderRadius: BorderRadius.circular(50)),
                child: TextField(
                  style: TextStyle(
                      color: CustomColors.darkText,
                      fontSize: SizeConfig.horizontalBlock * 3.5),
                  controller: nomorHPController,
                  keyboardType: TextInputType.number,
                  cursorColor: CustomColors.darkText,
                  textAlignVertical: TextAlignVertical.center,
                  textAlign: TextAlign.left,
                  decoration: InputDecoration(
                      isDense: true,
                      contentPadding: EdgeInsets.symmetric(
                          vertical: SizeConfig.verticalBlock * 2),
                      border: InputBorder.none,
                      hintText: 'Nomor HP',
                      hintStyle: TextStyle(color: CustomColors.darkText)),
                ),
              ),
            ),
            SizedBox(height: 15),
            Padding(
                padding: EdgeInsets.symmetric(horizontal: 25),
                child: CustomButton.button(context, 'Reset Password', () {
                  recoveryPassword();
                }, isPill: true, color: CustomButton.themeColor)),
            SizedBox(height: 25),
            Row(mainAxisAlignment: MainAxisAlignment.center, children: [
              Text('Belum punya akun? Registrasi ',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5)),
              SizedBox(width: 2),
              InkWell(
                onTap: () {
                  Navigator.pushReplacement(
                      context, MaterialPageRoute(builder: (_) => Register()));
                },
                child: Text('disini',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3.5,
                        fontWeight: FontWeight.w700,
                        color: CustomColors.green)),
              )
            ]),
            SizedBox(height: 10),
            InkWell(
              onTap: () {
                Navigator.pushReplacement(
                    context, MaterialPageRoute(builder: (_) => Login()));
              },
              child: Text('Login',
                  style: TextStyle(
                      fontSize: SizeConfig.horizontalBlock * 3.5,
                      fontWeight: FontWeight.w700,
                      color: CustomColors.green)),
            )
          ]),
    )));
  }
}
